package ru.pamparam.pw.clientcore.screens.gameplay.heroecs

import com.badlogic.ashley.core.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.minlog.Log
import com.esotericsoftware.spine.SkeletonRenderer
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.*
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems.*
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.packets.*

class HeroEcs(val gamePlay : GameplayScreen, val worldCamera : OrthographicCamera) {
    val engine : Engine
    private val localHeroRenderSystem: LocalHeroRenderSystem
    private val controllerSystemLocal: LocalHeroControllerSystem
    private val networkControllerSystem : NetworkControllerSystem
    private val networkRenderSystem : NetworkHeroRenderSystem
    private val bulletSystem : BulletUpdateAndRenderSystem

    private val mapHeroIdToEntity = mutableMapOf<Int, Entity>()

    val localHeroController = PamparamWorld.platformResolver.CreateHeroController(worldCamera, gamePlay.stage)
    private var localHeroEntity : Entity? = null

    init {
        engine = Engine()
        localHeroRenderSystem = LocalHeroRenderSystem(gamePlay)
        controllerSystemLocal = LocalHeroControllerSystem(gamePlay)
        networkControllerSystem = NetworkControllerSystem(gamePlay)
        networkRenderSystem = NetworkHeroRenderSystem(gamePlay)
        bulletSystem = BulletUpdateAndRenderSystem(gamePlay)

        engine.addSystem(localHeroRenderSystem)
        engine.addSystem(controllerSystemLocal)
        engine.addSystem(networkControllerSystem)
        engine.addSystem(networkRenderSystem)
        engine.addSystem(bulletSystem)
        engine.addEntityListener(object : EntityListener {
            override fun entityAdded(entity: Entity) {
                println("Entity added ${entity}")
            }

            override fun entityRemoved(entity: Entity) {
                println("Entity removed ${entity}")
            }
        })
    }

    fun update(timeDelta : Float) {
        engine.update(timeDelta)
    }

    fun draw(batch: SpriteBatch) {
        val positionMapper = ComponentMapper.getFor(HeroWorldPositionComponent::class.java)
        val animationMapper = ComponentMapper.getFor(HeroAnimationComponent::class.java)

        val entities = engine.getEntitiesFor(
                Family.all(
                        HeroWorldPositionComponent::class.java,
                        HeroAnimationComponent::class.java
                ).get()
        )
        val skeletonRenderer = SkeletonRenderer()
        for(entity in entities) {
            val position = positionMapper.get(entity)
            val animation = animationMapper.get(entity)

            animation.legsAnimation.render(position.toVector2(), MathUtils.radiansToDegrees * position.body.angle, Gdx.graphics.deltaTime, batch, skeletonRenderer)
            animation.bodyAnimation.render(position.toVector2(), MathUtils.radiansToDegrees * position.body.angle, Gdx.graphics.deltaTime, batch, skeletonRenderer)
        }

        ///

        val bulletMapper = ComponentMapper.getFor(BulletComponent::class.java)
        val bulletEntities = engine.getEntitiesFor(
                Family.all(
                        BulletComponent::class.java
                ).get()
        )
        for(entity in bulletEntities) {
            val bullet = bulletMapper.get(entity)

            val aff = Affine2()
            aff.translate(bullet.body.position)
            aff.rotate(bullet.body.angle * MathUtils.radiansToDegrees)
            val textureRegion = TextureRegion(bullet.texture)

            batch.draw(textureRegion, bullet.texture.width / gamePlay.PPM, bullet.texture.height / gamePlay.PPM, aff)
        }
    }

    fun handleSvpActorSync(serverPacket : SvpActorSync) {
        when(serverPacket) {
            is SvpLocalHeroInit -> onSvpLocalHeroInit(serverPacket)
            is SvpAddHero -> onSvpOtherHero(serverPacket)
            is SvpHeroState -> onSvpHeroState(serverPacket)
        }
    }

    private fun onSvpLocalHeroInit(svpHeroInit : SvpLocalHeroInit) {
        Log.info("Add local hero ${svpHeroInit.toString()}")
        val entity = Entity()

        entity.add(LocalHeroControllerComponent(localHeroController))
        entity.add(HeroWorldPositionComponent(gamePlay, svpHeroInit.x, svpHeroInit.y, svpHeroInit.rotation, true, entity))
        entity.add(HeroAnimationComponent(gamePlay.PPM))
        entity.add(LocalHeroWeaponComponent())
        entity.add(WeaponComponentLocalHeroActionQueue())
        engine.addEntity(entity)
        Gdx.input.inputProcessor = localHeroController as InputProcessor
        localHeroEntity = entity
        mapHeroIdToEntity[svpHeroInit.heroId] = entity
    }

    private fun onSvpOtherHero(svpOtherHeroInit: SvpAddHero) {
        val entity = Entity()

        entity.add(NetworkHeroControllerComponent())
        entity.add(HeroWorldPositionComponent(gamePlay, svpOtherHeroInit.x, svpOtherHeroInit.y, svpOtherHeroInit.rotation, false, entity))
        entity.add(HeroAnimationComponent(gamePlay.PPM))
        entity.add(NetworkHeroWeaponComponent())
        engine.addEntity(entity)
        mapHeroIdToEntity[svpOtherHeroInit.heroId] = entity
    }

    private fun onSvpHeroState(svpHeroState : SvpHeroState) {
        val entity = getEntityByHeroId(svpHeroState.heroId)
        if(entity == null)
            return

        val controller = entity.getComponent(NetworkHeroControllerComponent::class.java)
        if(controller == null)
            return

        val (weaponAction, weaponActionId) = when(svpHeroState) {
            is SvpHeroStateWeaponChange -> Pair(svpHeroState.weaponAction, svpHeroState.actionId)
            is SvpHeroStateWeaponReload -> Pair(WeaponActionType.reload, svpHeroState.actionId)
            is SvpHeroStateWeaponPrimaryAttack -> Pair(WeaponActionType.primaryAttack, svpHeroState.actionId)
            is SvpHeroStateWeaponSecondaryAttack -> Pair(WeaponActionType.secondaryAttack, svpHeroState.actionId)
            else -> Pair(WeaponActionType.none, 0)
        }

        val (targetId, attackSuccess) = when(svpHeroState) {
            is SvpHeroStateWeaponPrimaryAttack -> Pair(svpHeroState.targetActorId, svpHeroState.success)
            is SvpHeroStateWeaponSecondaryAttack -> Pair(svpHeroState.targetActorId, svpHeroState.success)
            else -> Pair(0, false)
        }

        controller.addState(
                NetworkHeroControllerComponent.State(
                serverQuantNumber = gamePlay.serverQuantNumber,
                x = svpHeroState.x,
                y = svpHeroState.y,
                moveRotation = svpHeroState.moveRotation,
                lookRotation = svpHeroState.lookRotation,
                isMove = svpHeroState.isMove,
                weaponAction = weaponAction,
                weaponActionId = weaponActionId,
                targetId = targetId
                )
        )
    }

    fun getEntityByHeroId(heroId : Int) : Entity? {
        return mapHeroIdToEntity.get(heroId)
    }

    fun getLocalHeroPosition() : Vector2 =
        localHeroEntity?.getComponent(HeroWorldPositionComponent::class.java)?.toVector2() ?: Vector2(0f,0f)

}