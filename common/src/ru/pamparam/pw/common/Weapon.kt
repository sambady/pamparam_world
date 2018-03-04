package ru.pamparam.pw.common

enum class WeaponActionType
{
    none,
    selectKnife,
    selectPistol,
    selectRifle,
    primaryAttack,
    secondaryAttack,
    reload
}

fun WeaponTypeByWeaponAction(weaponActionType: WeaponActionType) : WeaponType {
    return when(weaponActionType) {
        WeaponActionType.selectKnife -> WeaponType.knife
        WeaponActionType.selectPistol -> WeaponType.pistol
        WeaponActionType.selectRifle -> WeaponType.rifle
        else -> WeaponType.none
    }
}

enum class WeaponType(val idx : Int,
                      val availableCommand: Array<WeaponActionType>,
                      val animationName : String,
                      val primaryAttack : String,
                      val secondaryAttack : String,
                      val clipSize : Int,
                      val primaryAttackDuration : Float,
                      val secondaryAttackDuration : Float,
                      val reloadDuration : Float,
                      val changeDuration : Float)
{
    none(0,
            arrayOf(),
            "",
            "",
            "",
            0,
            0.0f,
            0.0f,
            0.0f,
            0.0f
    ),
    knife(1,
            arrayOf(WeaponActionType.primaryAttack, WeaponActionType.secondaryAttack),
            "knife",
            "meleeattack",
            "meleeattack",
            0,
            0.5f,
            0.5f,
            0.0f,
            0.1f
    ),
    pistol(2,
            arrayOf(WeaponActionType.reload, WeaponActionType.primaryAttack, WeaponActionType.secondaryAttack),
            "handgun",
            "shoot",
            "shoot",
            10,
            0.2f,
            0.2f,
            0.5f,
            0.5f),
    rifle(3,
            arrayOf(WeaponActionType.reload, WeaponActionType.primaryAttack, WeaponActionType.secondaryAttack),
            "rifle",
            "shoot",
            "meleeattack",
            5,
            0.4f,
            0.7f,
            0.5f,
            0.5f)
}