package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.RayCastCallback
import com.badlogic.gdx.physics.box2d.World
import com.esotericsoftware.minlog.Log
import org.apache.commons.lang3.builder.EqualsBuilder
import ru.pamparam.pw.clientcore.AnimationHelpers
import ru.pamparam.pw.clientcore.AnimationHelpers.getWeaponPositionAndDirection
import ru.pamparam.pw.clientcore.PositionHelpers
import ru.pamparam.pw.clientcore.Pw
import ru.pamparam.pw.clientcore.normolizeAngle
import ru.pamparam.pw.clientcore.screens.gameplay.Bullet
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

class LocalHeroControllerSystem(val gameplay : GameplayScreen) : EntitySystem() {
    lateinit var entities: ImmutableArray<Entity>

    private val positionMapper = ComponentMapper.getFor(HeroWorldPositionComponent::class.java)
    private val controllerMapper = ComponentMapper.getFor(LocalHeroControllerComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(LocalHeroWeaponComponent::class.java)
    private val weaponActionQueueMapper = ComponentMapper.getFor(WeaponComponentLocalHeroActionQueue::class.java)
    private val animationMapper = ComponentMapper.getFor(HeroAnimationComponent::class.java)

    override fun addedToEngine(engine: Engine?) {
        if(engine == null)
            return
        entities = engine.getEntitiesFor(
                Family.all(
                        HeroWorldPositionComponent::class.java,
                        LocalHeroControllerComponent::class.java,
                        LocalHeroWeaponComponent::class.java,
                        WeaponComponentLocalHeroActionQueue::class.java,
                        HeroAnimationComponent::class.java
                ).get()
        )
    }

    override fun update(deltaTime: Float) {
        for (entity in entities) {
            val position = positionMapper.get(entity)
            val controller = controllerMapper.get(entity)
            val weapon = weaponMapper.get(entity)
            val weaponActionQueue = weaponActionQueueMapper.get(entity)
            val animation = animationMapper.get(entity)

            try {

                UpdatePosition(controller, position)
                UpdateWeapon(deltaTime, controller, weapon, weaponActionQueue, position, animation)
                //gameplay.box2dWorld.rayCast(object RayCastCallback {

                //}, position.body.position, position.body.position)
                val newHeroState = BuildClpState(x = position.body.position.x,
                        y = position.body.position.y,
                        moveRotation = controller.controller.getMoveInfo().moveRotation,
                        lookRotation = controller.controller.getLookRotation(position.toVector2()),
                        weaponAction = weapon.action,
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
                             weaponActionQueue : WeaponComponentLocalHeroActionQueue,
                             locatlHeroPosition: HeroWorldPositionComponent,
                             animationComponent: HeroAnimationComponent)
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

        UpdateCurrentWeapon(deltaTime, localHeroWeapon, weaponActionQueue, locatlHeroPosition, animationComponent)
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
        if(newWeaponType == localHeroWeapon.weaponType)
            return
        // On change weapon - clear all actions.
        if(localHeroWeapon.action != WeaponActionType.none) {
            EndAction(localHeroWeapon)
        }
        StartAction(localHeroWeapon, actionType)
        weaponActionQueue.actionQueue.clear()
    }

    private fun UpdateCurrentWeapon(deltaTime: Float,
                                    localHeroWeapon: LocalHeroWeaponComponent,
                                    weaponActionQueue : WeaponComponentLocalHeroActionQueue,
                                    locatlHeroPosition : HeroWorldPositionComponent,
                                    animationComponent: HeroAnimationComponent) : WeaponActionType
    {
        if(localHeroWeapon.action != WeaponActionType.none) {
            Pw.setDebug1("${localHeroWeapon.action.name} ${localHeroWeapon.actionTimeLeft}")
            localHeroWeapon.actionTimeLeft -= deltaTime
            if(localHeroWeapon.actionTimeLeft < 0.0) {
                EndAction(localHeroWeapon)
                Pw.setDebug1("")
            }
        }
        else {
            if(weaponActionQueue.actionQueue.isNotEmpty()) {
                val action = weaponActionQueue.actionQueue.first()
                weaponActionQueue.actionQueue.removeAt(0)
                StartAction(localHeroWeapon, action)
                // TODO DoAttack

                val angle = MathUtils.radiansToDegrees * locatlHeroPosition.body.angle
                val direction = Vector2(1f, 0f)
                direction.rotate(angle)
                direction.scl(1000f, 1000f)
                direction.add(locatlHeroPosition.toVector2())

                Log.info("Position ${locatlHeroPosition.toVector2()} Direction ${direction} Angle ${angle}")
                if(action == WeaponActionType.primaryAttack) {
                    var closestFixture : Fixture? = null
                    var closestFraction = 0f
                    gameplay.box2dWorld.rayCast(object : RayCastCallback {
                        override fun reportRayFixture(fixture: Fixture?, point: Vector2?, normal: Vector2?, fraction: Float): Float {
                            if(closestFraction == 0f || fraction < closestFraction) {
                                closestFraction = fraction
                                closestFixture = fixture
                            }
                            return 1f
                        }
                    }, locatlHeroPosition.toVector2(), direction)




                    val (position, rotation) = AnimationHelpers.getWeaponPositionAndDirection(animationComponent.bodyAnimation.skeleton)

                    val entity = Entity()
                    entity.add(BulletComponent(gameplay, position, rotation))
                    gameplay.localHeroEcs.engine.addEntity(entity)


                    //Log.info("Point ${offset} position ${rotation}")
                }

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
        val rotation = controller.controller.getLookRotation(positionHero.toVector2())
        val (runDest, positionDelta) = if(!moveDirection.isMove) {
            Pair(RunDest.IDLE, Vector2(0f, 0f))
        }
            else {
            var angleDiff = normolizeAngle(moveDirection.moveRotation - rotation)
            val runDest = PositionHelpers.angleToRunDestination(angleDiff)
            val speed = PositionHelpers.speedFromRunDestination(positionHero.runDestination)
            val positionDelta = Vector2(speed, 0f).rotate(moveDirection.moveRotation)
            Pair(runDest, Vector2(positionDelta.x, positionDelta.y))
        }

        positionHero.runDestination = runDest
        positionHero.body.setLinearVelocity(positionDelta.x, positionDelta.y)
        positionHero.body.setTransform(positionHero.body.position, MathUtils.degreesToRadians * rotation)
    }

    fun StartAction(weaponComponent: LocalHeroWeaponComponent, action : WeaponActionType) {
        weaponComponent.action = action
        weaponComponent.weaponType = when(action) {
            WeaponActionType.selectKnife -> WeaponType.knife
            WeaponActionType.selectPistol -> WeaponType.pistol
            WeaponActionType.selectRifle -> WeaponType.rifle
            else -> weaponComponent.weaponType
        }

        weaponComponent.actionTimeLeft = when(action) {
            WeaponActionType.selectKnife -> WeaponType.knife.changeDuration
            WeaponActionType.selectPistol -> WeaponType.pistol.changeDuration
            WeaponActionType.selectRifle -> WeaponType.rifle.changeDuration
            WeaponActionType.reload -> weaponComponent.weaponType.reloadDuration
            WeaponActionType.primaryAttack -> weaponComponent.weaponType.primaryAttackDuration
            WeaponActionType.secondaryAttack -> weaponComponent.weaponType.secondaryAttackDuration
            else -> 0.0f
        }
        weaponComponent.actionId += 1
    }

    fun EndAction(weaponComponent : LocalHeroWeaponComponent) {
        weaponComponent.actionTimeLeft = 0.0f
        weaponComponent.action = WeaponActionType.none
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