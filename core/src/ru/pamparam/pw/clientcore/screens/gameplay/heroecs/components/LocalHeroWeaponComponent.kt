package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType

data class WeaponRenderInfo(val weaponType: WeaponType, val weaponAction : WeaponActionType)

class WeaponComponentLocalHeroActionQueue : Component
{
    val actionQueue = mutableListOf<WeaponActionType>()
}

class LocalHeroWeaponComponent : Component
{
    var weaponType = WeaponType.knife
    var actionTimeLeft = 0.0f
    var action = WeaponActionType.none
    var actionId = 0
}
