package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.minlog.Log
import org.apache.commons.lang3.builder.EqualsBuilder
import ru.pamparam.pw.clientcore.PositionHelpers
import ru.pamparam.pw.clientcore.Pw
import ru.pamparam.pw.clientcore.normolizeAngle
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.*
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.LocalHeroControllerComponent
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.RunDest
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.LocalHeroWeaponComponent
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.WeaponComponentLocalHeroActionQueue
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.HeroWorldPositionComponent
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType
import ru.pamparam.pw.packets.*

class LocalHeroControllerSystem(val gameplay: GameplayScreen) : EntitySystem() {
    lateinit var entities: ImmutableArray<Entity>

    private val positionMapper = ComponentMapper.getFor(HeroWorldPositionComponent::class.java)
    private val controllerMapper = ComponentMapper.getFor(LocalHeroControllerComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(LocalHeroWeaponComponent::class.java)
    private val weaponActionQueueMapper = ComponentMapper.getFor(WeaponComponentLocalHeroActionQueue::class.java)

    override fun addedToEngine(engine: Engine?) {
        if(engine == null)
            return
        entities = engine.getEntitiesFor(
                Family.all(
                        HeroWorldPositionComponent::class.java,
                        LocalHeroControllerComponent::class.java,
                        LocalHeroWeaponComponent::class.java,
                        WeaponComponentLocalHeroActionQueue::class.java
                ).get()
        )
    }

    override fun update(deltaTime: Float) {
        for (entity in entities) {
            val position = positionMapper.get(entity)
            val controller = controllerMapper.get(entity)
            val weapon = weaponMapper.get(entity)
            val weaponActionQueue = weaponActionQueueMapper.get(entity)

            try {
                UpdatePosition(controller, position)
                val weaponAction = UpdateWeapon(deltaTime, controller, weapon, weaponActionQueue)

                val newHeroState = BuildClpState(x = position.x,
                        y = position.y,
                        moveRotation = controller.controller.getMoveInfo().moveRotation,
                        lookRotation = controller.controller.getLookRotation(position.toVector2()),
                        weaponAction = weaponAction,
                        isMove = controller.controller.getMoveInfo().isMove,
                        actionId = weapon.actionId
                )
                if(!EqualsBuilder.reflectionEquals(controller.lastSendState, newHeroState)) {
                    Pw.sendPacket(newHeroState)
                    controller.lastSendState = newHeroState
                }
            }
            catch(assert : AssertionError) {
                Log.error("Error update local hero", assert)
            }
        }
    }

    //-----------------------------

    private fun UpdateWeapon(deltaTime: Float,
                             controller: LocalHeroControllerComponent,
                             localHeroWeapon: LocalHeroWeaponComponent,
                             weaponActionQueue : WeaponComponentLocalHeroActionQueue) : WeaponActionType
    {
        val newWeaponAction = controller.controller.getAndFlushWeaponCommand()

        if(newWeaponAction != WeaponActionType.none) {
            when(newWeaponAction) {
                WeaponActionType.selectKnife,
                WeaponActionType.selectRifle,
                WeaponActionType.selectPistol -> tryChangeWeapon(localHeroWeapon, newWeaponAction, weaponActionQueue)
                else -> TryAddActionToQueue(newWeaponAction, weaponActionQueue)
            }

            // TODO Update action queue....
            //weaponActionQueue.actionQueue.add(newWeaponAction)
        }

        UpdateCurrentWeapon(deltaTime, localHeroWeapon, weaponActionQueue)
        return localHeroWeapon.currentWeaponAction
    }

    private fun tryChangeWeapon(localHeroWeapon: LocalHeroWeaponComponent,
                                actionType: WeaponActionType,
                                weaponActionQueue : WeaponComponentLocalHeroActionQueue)
    {
        val newWeaponType = when(actionType) {
            WeaponActionType.selectKnife -> WeaponType.knife
            WeaponActionType.selectRifle -> WeaponType.rifle
            WeaponActionType.selectPistol -> WeaponType.pistol
            else -> throw Exception("Unknown reload weapon command")
        }
        if(newWeaponType == localHeroWeapon.currentWeaponType)
            return
        // On change weapon - clear all actions.
        if(localHeroWeapon.currentWeaponAction != WeaponActionType.none) {
            localHeroWeapon.EndAction()
        }
        localHeroWeapon.StartAction(actionType)
        weaponActionQueue.actionQueue.clear()
    }

    private fun UpdateCurrentWeapon(deltaTime: Float,
                                    localHeroWeapon: LocalHeroWeaponComponent,
                                    weaponActionQueue : WeaponComponentLocalHeroActionQueue) : WeaponActionType
    {
        if(localHeroWeapon.currentWeaponAction != WeaponActionType.none) {
            Pw.setDebug1("${localHeroWeapon.currentWeaponAction.name} ${localHeroWeapon.currentWeaponActionTimeLeft}")
            localHeroWeapon.currentWeaponActionTimeLeft -= deltaTime
            if(localHeroWeapon.currentWeaponActionTimeLeft < 0.0) {
                localHeroWeapon.EndAction()
                Pw.setDebug1("")
            }
        }
        else {
            if(weaponActionQueue.actionQueue.isNotEmpty()) {
                val action = weaponActionQueue.actionQueue.first()
                weaponActionQueue.actionQueue.removeAt(0)
                localHeroWeapon.StartAction(action)
                return action
            }
        }

        return WeaponActionType.none
    }

    private fun TryAddActionToQueue(weaponAction : WeaponActionType,
                                    weaponQueue : WeaponComponentLocalHeroActionQueue)
    {
        weaponQueue.actionQueue.add(weaponAction)
    }

    //-----------------------------

    private fun UpdatePosition(controller : LocalHeroControllerComponent, positionHero: HeroWorldPositionComponent) {
        val moveDirection = controller.controller.getMoveInfo()
        positionHero.rotation = controller.controller.getLookRotation(positionHero.toVector2())
        if(!moveDirection.isMove) {
            positionHero.runDestination = RunDest.IDLE
            return
        }

        val positionDelta = Vector2(1f, 0f).rotate(moveDirection.moveRotation)
        var angleDiff = normolizeAngle(moveDirection.moveRotation - positionHero.rotation)

        positionHero.runDestination = PositionHelpers.angleToRunDestination(angleDiff)
        val speed = PositionHelpers.speedFromRunDestination(positionHero.runDestination)

        positionHero.x += positionDelta.x * speed
        positionHero.y += positionDelta.y * speed
    }

    private fun BuildClpState(x : Float,
                              y : Float,
                              moveRotation : Float,
                              lookRotation : Float,
                              isMove : Boolean,
                              weaponAction : WeaponActionType = WeaponActionType.none,
                              actionId : Int) : ClpHeroState
    {
        return when(weaponAction) {
            WeaponActionType.selectRifle,
            WeaponActionType.selectPistol,
            WeaponActionType.selectKnife -> {
                ClpHeroStateWeaponChange().let {
                    it.x = x
                    it.y = y
                    it.moveRotation = moveRotation
                    it.lookRotation = lookRotation
                    it.isMove = isMove
                    it.weaponAction = weaponAction
                    it.actionId = actionId
                    it
                }
            }
            WeaponActionType.reload -> {
                ClpHeroStateWeaponReload().let {
                    it.x = x
                    it.y = y
                    it.moveRotation = moveRotation
                    it.lookRotation = lookRotation
                    it.isMove = isMove
                    it.actionId = actionId
                    it
                }
            }
            WeaponActionType.primaryAttack -> {
                ClpHeroStateWeaponPrimaryAttack().let {
                    it.x = x
                    it.y = y
                    it.moveRotation = moveRotation
                    it.lookRotation = lookRotation
                    it.isMove = isMove
                    it.actionId = actionId
                    it
                }
            }
            WeaponActionType.secondaryAttack -> {
                ClpHeroStateWeaponSecondaryAttack().let {
                    it.x = x
                    it.y = y
                    it.moveRotation = moveRotation
                    it.lookRotation = lookRotation
                    it.isMove = isMove
                    it.actionId = actionId
                    it
                }
            }
            WeaponActionType.none -> ClpHeroState(
                    x = x,
                    y = y,
                    moveRotation = moveRotation,
                    lookRotation = lookRotation,
                    isMove = isMove)
        }
    }
}