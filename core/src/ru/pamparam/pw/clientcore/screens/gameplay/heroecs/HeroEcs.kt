package ru.pamparam.pw.clientcore.screens.gameplay.heroecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.*
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems.LocalHeroControllerSystem
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems.LocalHeroRenderSystem
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems.NetworkControllerSystem
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems.NetworkHeroRenderSystem
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.packets.*


class HeroEcs(val gamePlay : GameplayScreen, val worldCamera : OrthographicCamera) {
    private val engine : Engine
    private val localHeroRenderSystem: LocalHeroRenderSystem
    private val controllerSystemLocal: LocalHeroControllerSystem
    private val networkControllerSystem : NetworkControllerSystem
    private val networkRenderSystem : NetworkHeroRenderSystem
    private val mapHeroIdToEntity = mutableMapOf<Int, Entity>()

    val localHeroController = PamparamWorld.platformResolver.CreateHeroController(worldCamera)
    private var localHeroEntity : Entity? = null

    init {
        engine = Engine()
        localHeroRenderSystem = LocalHeroRenderSystem(gamePlay)
        controllerSystemLocal = LocalHeroControllerSystem(gamePlay)
        networkControllerSystem = NetworkControllerSystem(gamePlay)
        networkRenderSystem = NetworkHeroRenderSystem(gamePlay)
        engine.addSystem(localHeroRenderSystem)
        engine.addSystem(controllerSystemLocal)
        engine.addSystem(networkControllerSystem)
        engine.addSystem(networkRenderSystem)
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
        entity.add(HeroWorldPositionComponent(svpHeroInit.x, svpHeroInit.y, svpHeroInit.rotation))
        entity.add(HeroAnimationComponent())
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
        entity.add(HeroWorldPositionComponent(svpOtherHeroInit.x, svpOtherHeroInit.y, svpOtherHeroInit.rotation))
        entity.add(HeroAnimationComponent())
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