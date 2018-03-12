package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.esotericsoftware.spine.SkeletonRenderer
import ru.pamparam.pw.clientcore.AnimationHelpers
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.HeroAnimationComponent
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.LocalHeroWeaponComponent
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.HeroWorldPositionComponent
import ru.pamparam.pw.common.WeaponActionType


class LocalHeroRenderSystem(val gameplay: GameplayScreen) : EntitySystem() {
    lateinit var entities: ImmutableArray<Entity>

    private val positionMapper = ComponentMapper.getFor(HeroWorldPositionComponent::class.java)
    private val animationMapper = ComponentMapper.getFor(HeroAnimationComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(LocalHeroWeaponComponent::class.java)

    override fun addedToEngine(engine: Engine?) {
        entities = engine!!.getEntitiesFor(
                Family.all(
                        HeroWorldPositionComponent::class.java,
                        HeroAnimationComponent::class.java,
                        LocalHeroWeaponComponent::class.java
                ).get())
    }

    private val skeletonRenderer = SkeletonRenderer()

    override fun update(deltaTime: Float) {
        for (entity in entities) {
            val position = positionMapper.get(entity)
            val weapon = weaponMapper.get(entity)
            val animation = animationMapper.get(entity)

            updateAnimation(position, weapon, animation)
            render(position, animation)
        }
    }

    private fun updateAnimation(positionHero: HeroWorldPositionComponent, weapon : LocalHeroWeaponComponent, animation : HeroAnimationComponent) {
        // Update leg animation:
        if(positionHero.runDestination != animation.runDestination) {
            animation.legsAnimation.animationState.setAnimation(0, AnimationHelpers.getLegLoopAnimationString(positionHero.runDestination), true)
        }

        // Update body and weapon animation:
        val currentActionAnimation = animation.actionAnimation
        if(currentActionAnimation != null && currentActionAnimation.isComplete) {
            // End old action. switch to loop animation
            animation.bodyAnimation.animationState.setAnimation(
                    0, AnimationHelpers.getBodyLoopAnimationString(positionHero.runDestination, animation.weaponType), true)
            animation.actionAnimation = null
        }

        if(weapon.actionId != animation.weaponActionId) {
            // Start new action
            assert(weapon.action != WeaponActionType.none) {
                "ActionType = ${WeaponActionType.none} for actionId ${weapon.actionId}"
            }
            animation.actionAnimation = animation.bodyAnimation.animationState.setAnimation(0,
                    AnimationHelpers.getBodyWeaponActionAnimationString(weapon.weaponType, weapon.action),
                    false)
            animation.weaponActionId = weapon.actionId
        }


        if(animation.actionAnimation  == null){
            if ((positionHero.runDestination != animation.runDestination) || (weapon.weaponType != animation.weaponType)) {
                animation.bodyAnimation.animationState.setAnimation(0, AnimationHelpers.getBodyLoopAnimationString(positionHero.runDestination, weapon.weaponType), true)
            }
        }

        animation.weaponType = weapon.weaponType
        animation.runDestination = positionHero.runDestination
    }

    private fun render(positionHero: HeroWorldPositionComponent, heroAnimation : HeroAnimationComponent) {
        heroAnimation.legsAnimation.render(positionHero.toVector2(), MathUtils.radiansToDegrees * positionHero.body.angle, Gdx.graphics.deltaTime, gameplay.worldSpriteBatch, skeletonRenderer)
        heroAnimation.bodyAnimation.render(positionHero.toVector2(), MathUtils.radiansToDegrees * positionHero.body.angle, Gdx.graphics.deltaTime, gameplay.worldSpriteBatch, skeletonRenderer)
    }
}

