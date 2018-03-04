package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType

data class WeaponRenderInfo(val weaponType: WeaponType, val weaponAction : WeaponActionType)
fun LocalHeroWeaponComponent.GetRenderInfo() = WeaponRenderInfo(currentWeaponType, currentWeaponAction)

class WeaponComponentLocalHeroActionQueue : Component
{
    val actionQueue = mutableListOf<WeaponActionType>()
}

class LocalHeroWeaponComponent : Component
{
    var currentWeaponType = WeaponType.knife
    var currentWeaponActionTimeLeft = 0.0f
    var currentWeaponAction = WeaponActionType.none
    var actionId = 0
}

fun LocalHeroWeaponComponent.StartAction(action : WeaponActionType) {
    currentWeaponAction = action
    currentWeaponType = when(action) {
        WeaponActionType.selectKnife -> WeaponType.knife
        WeaponActionType.selectPistol -> WeaponType.pistol
        WeaponActionType.selectRifle -> WeaponType.rifle
        else -> currentWeaponType
    }

    currentWeaponActionTimeLeft = when(action) {
        WeaponActionType.selectKnife -> WeaponType.knife.changeDuration
        WeaponActionType.selectPistol -> WeaponType.pistol.changeDuration
        WeaponActionType.selectRifle -> WeaponType.rifle.changeDuration
        WeaponActionType.reload -> currentWeaponType.reloadDuration
        WeaponActionType.primaryAttack -> currentWeaponType.primaryAttackDuration
        WeaponActionType.secondaryAttack -> currentWeaponType.secondaryAttackDuration
        else -> 0.0f
    }
    actionId += 1
}

fun LocalHeroWeaponComponent.EndAction() {
    currentWeaponActionTimeLeft = 0.0f
    currentWeaponAction = WeaponActionType.none
}