package ru.pamparam.pw.clientcore

import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.RunDest
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType

object AnimationHelpers {
    fun getBodyLoopAnimationString(runDest_: RunDest, weaponType_: WeaponType): String {
        return when (runDest_) {
            RunDest.IDLE -> "idle_"
            else -> "move_"
        } + weaponType_.animationName
    }

    fun getLegLoopAnimationString(runDest_: RunDest): String = when (runDest_) {
        RunDest.IDLE -> "idle"
        RunDest.FRONT -> "run"
        RunDest.FRONT_LEFT -> "run"
        RunDest.FRONT_RIGHT -> "run"
        RunDest.LEFT -> "strafe_left"
        RunDest.RIGHT -> "strafe_right"
        RunDest.BACK -> "walk"
        RunDest.BACK_LEFT -> "walk"
        RunDest.BACK_RIGHT -> "walk"
    }

    fun getBodyWeaponActionAnimationString(weaponType_: WeaponType, weaponActionType_: WeaponActionType): String? {
        return when (weaponActionType_) {
            WeaponActionType.selectKnife -> "idle_" + WeaponType.knife.animationName
            WeaponActionType.selectPistol -> "reload_" + WeaponType.pistol.animationName
            WeaponActionType.selectRifle -> "reload_" + WeaponType.rifle.animationName
            else -> {
                when (weaponActionType_) {
                    WeaponActionType.primaryAttack -> weaponType_.primaryAttack
                    WeaponActionType.secondaryAttack -> weaponType_.secondaryAttack
                    WeaponActionType.reload -> "reload"
                    else -> "idle"
                } + "_" + weaponType_.animationName
            }
        }
    }
}
