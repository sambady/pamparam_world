package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import com.esotericsoftware.spine.AnimationState
import ru.pamparam.pw.clientcore.AnimationHelpers
import ru.pamparam.pw.clientcore.PwHeroAnimation
import ru.pamparam.pw.common.WeaponType

class HeroAnimationComponent() : Component
{
    val bodyAnimation by lazy {
        val animation = PwHeroAnimation("survivor/body/survivor.atlas", "survivor/body/survivor.json")
        animation.animationState.setAnimation(0, AnimationHelpers.getBodyLoopAnimationString(runDestination, weaponType), true)
        animation
    }
    val legsAnimation by lazy {
        val animation = PwHeroAnimation("survivor/legs/survivor.atlas", "survivor/legs/survivor.json")
        animation.animationState.setAnimation(0, AnimationHelpers.getLegLoopAnimationString(runDestination), true)
        animation
    }

    var runDestination = RunDest.IDLE
    var weaponType = WeaponType.knife
    var weaponActionId = 0

    var actionAnimation : AnimationState.TrackEntry? = null
}