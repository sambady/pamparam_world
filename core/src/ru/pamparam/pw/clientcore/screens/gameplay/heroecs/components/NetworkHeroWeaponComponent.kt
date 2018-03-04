package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import ru.pamparam.pw.common.WeaponActionType
import ru.pamparam.pw.common.WeaponType

class NetworkHeroWeaponComponent : Component {
    var weaponType = WeaponType.knife
    var weaponAction = WeaponActionType.none
    var actionId = 0
}