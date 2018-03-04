package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.Gdx
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.clientcore.PositionHelpers
import ru.pamparam.pw.clientcore.normolizeAngle
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.*
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType

class NetworkControllerSystem(val gameplay: GameplayScreen) : EntitySystem() {
    lateinit var entities: ImmutableArray<Entity>

    private val positionMapper = ComponentMapper.getFor(HeroWorldPositionComponent::class.java)
    private val controllerMapper = ComponentMapper.getFor(NetworkHeroControllerComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(NetworkHeroWeaponComponent::class.java)

    override fun addedToEngine(engine: Engine?) {
        if(engine == null)
            return
        entities = engine.getEntitiesFor(Family.all(HeroWorldPositionComponent::class.java,
                NetworkHeroControllerComponent::class.java,
                NetworkHeroWeaponComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        val quantWeWillShow = gameplay.serverQuantNumber - Gdx.app.getPreferences("sync").getInteger("quant_delay")
        for (entity in entities) {
            val position = positionMapper.get(entity)
            val controller = controllerMapper.get(entity)
            val weapon = weaponMapper.get(entity)

            UpdatePosition(quantWeWillShow, controller, position, weapon)
        }
    }

    private fun UpdatePosition(serverQuantNumberToRender : Int, controller : NetworkHeroControllerComponent, positionHero: HeroWorldPositionComponent, weapon: NetworkHeroWeaponComponent) {
        var lastStateWeKnow : NetworkHeroControllerComponent.State? = null
        var nextQuantWeKnow : NetworkHeroControllerComponent.State? = null
        var state : NetworkHeroControllerComponent.State? = null

        if(controller.stateQueue.isEmpty())
            return

        for(it in controller.stateQueue) {
            if(it.serverQuantNumber == serverQuantNumberToRender) {
                state = it
                break
            }
            if(it.serverQuantNumber < serverQuantNumberToRender) {
                lastStateWeKnow = it
            }
            if(it.serverQuantNumber > serverQuantNumberToRender) {
                nextQuantWeKnow = it
                break
            }
        }
/*
        val firstQuant = controller.stateQueue.first.serverQuantNumber
        val lastQuant = controller.stateQueue.last.serverQuantNumber
        val lastFound = lastStateWeKnow?.serverQuantNumber ?: 0
        val nextFound = nextQuantWeKnow?.serverQuantNumber ?: 0
        val stateFound = state?.serverQuantNumber ?: 0

        gameplay.debugLabel1.text = "$quantWeWillShow [$firstQuant:$lastQuant] [$lastFound:$nextFound] $stateFound"
*/

        // TODO If state not found
        /*if(state == null && nextQuantWeKnow != null) {
            if(lastStateWeKnow == null) {
                return
            }
        }*/

        if(state == null)
            return

        positionHero.rotation = state.lookRotation
        if(state.targetId != 0) {
            val targetPosition = gameplay.heroEcs.getEntityByHeroId(state.targetId)?.
                    getComponent(HeroWorldPositionComponent::class.java)?.toVector2()
            positionHero.rotation = positionHero.toVector2().angle(targetPosition)
        }

        positionHero.x = state.x
        positionHero.y = state.y

        positionHero.runDestination = when(state.isMove) {
            true -> PositionHelpers.angleToRunDestination(normolizeAngle(state.moveRotation - positionHero.rotation))
            false -> RunDest.IDLE
        }

        when(state.weaponAction) {
            WeaponActionType.selectRifle -> weapon.weaponType = WeaponType.rifle
            WeaponActionType.selectPistol -> weapon.weaponType = WeaponType.pistol
            WeaponActionType.selectKnife -> weapon.weaponType = WeaponType.knife
        }

        if(state.weaponAction != WeaponActionType.none && weapon.actionId != state.weaponActionId) {
            weapon.weaponAction = state.weaponAction
            weapon.actionId = state.weaponActionId
        }

        controller.lastUpdatedServerQuant = serverQuantNumberToRender

        while(controller.stateQueue.isNotEmpty() && controller.stateQueue.first.serverQuantNumber <= serverQuantNumberToRender) {
            controller.stateQueue.pop()
        }
    }
}