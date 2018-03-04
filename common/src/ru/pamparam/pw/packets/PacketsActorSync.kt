package ru.pamparam.pw.packets

import com.esotericsoftware.kryo.Kryo
import ru.pamparam.pw.common.WeaponActionType
import kotlin.reflect.KClass


sealed class ClpActorSync : ClientPacket()
sealed class SvpActorSync : ServerPacket()

fun RegisterActorSyncPackets(prefix : Int, kryo : Kryo) {
    var idx = 1
    fun <T : Any>registerPacket(packet : KClass<T>) {
        println("RegisterActorSyncPackets ${packet.toString()} ${prefix + idx}")
        kryo.register(packet.java, prefix + idx)
        idx += 1
    }
    registerPacket(SvpLocalHeroInit::class)
    registerPacket(SvpAddHero::class)
    registerPacket(ClpHeroState::class)
    registerPacket(SvpHeroState::class)
    registerPacket(ClpWeaponCommand::class)
    registerPacket(SvpWeaponCommand::class)
    registerPacket(ProtoWeaponAction::class)

    registerPacket(ClpHeroStateWeaponChange::class)
    registerPacket(SvpHeroStateWeaponChange::class)

    registerPacket(ClpHeroStateWeaponReload::class)
    registerPacket(SvpHeroStateWeaponReload::class)

    registerPacket(ClpHeroStateWeaponPrimaryAttack::class)
    registerPacket(SvpHeroStateWeaponPrimaryAttack::class)
    registerPacket(ClpHeroStateWeaponSecondaryAttack::class)
    registerPacket(SvpHeroStateWeaponSecondaryAttack::class)
}

data class SvpLocalHeroInit(
        val heroId : Int = 0,
        val x : Float = 0f,
        val y : Float = 0f,
        val rotation : Float = 0f) : SvpActorSync()

data class SvpAddHero(
        val heroId : Int = 0,
        val x : Float = 0f,
        val y : Float = 0f,
        val rotation : Float = 0f,
        val isMove : Boolean = false) : SvpActorSync()

open class ClpHeroState(
        var serverQuantNumber : Int = 0,
        var clientQuantNumber : Int = 0,
        var x : Float = 0f,
        var y : Float = 0f,
        var moveRotation : Float = 0f,
        var lookRotation: Float = 0f,
        var isMove : Boolean = false) : ClpActorSync()

open class SvpHeroState(
        var serverQuantNumber : Int = 0,
        var heroId : Int = 0,
        var x : Float = 0f,
        var y : Float = 0f,
        var moveRotation : Float = 0f,
        var lookRotation: Float = 0f,
        var isMove : Boolean = false) : SvpActorSync()

class ClpHeroStateWeaponChange(var weaponAction : WeaponActionType = WeaponActionType.none, var actionId : Int = 0) : ClpHeroState()
class SvpHeroStateWeaponChange(var weaponAction : WeaponActionType = WeaponActionType.none, var actionId : Int = 0) : SvpHeroState()

class ClpHeroStateWeaponReload(var actionId : Int = 0) : ClpHeroState()
class SvpHeroStateWeaponReload(var actionId : Int = 0) : SvpHeroState()

class ClpHeroStateWeaponPrimaryAttack(var targetActorId : Int = 0, var success : Boolean = false, var actionId : Int = 0) : ClpHeroState()
class SvpHeroStateWeaponPrimaryAttack(var targetActorId : Int = 0, var success : Boolean = false, var actionId : Int = 0) : SvpHeroState()

class ClpHeroStateWeaponSecondaryAttack(var targetActorId : Int = 0, var success : Boolean = false, var actionId : Int = 0) : ClpHeroState()
class SvpHeroStateWeaponSecondaryAttack(var targetActorId : Int = 0, var success : Boolean = false, var actionId : Int = 0) : SvpHeroState()

enum class ProtoWeaponAction(val idx : Int) {
    NO_ACTION(0),
    CHANGE_WEAPON(1),
    RELOAD(2),
    ATTACK_PRIMARY(3),
    ATTACK_SECONDARY(4)
}

data class ClpWeaponCommand (
        val serverQuantNumber : Int = 0,
        val clientQuantNumber : Int = 0,
        val weaponAction : ProtoWeaponAction = ProtoWeaponAction.NO_ACTION,
        val weaponType : Int = 0,
        val targetHeroId : Int = 0,
        val result : Int = 0
) : ClpActorSync()

data class SvpWeaponCommand(
        val serverQuantNumber : Int = 0,
        val heroId : Int = 0,
        val weaponAction : ProtoWeaponAction = ProtoWeaponAction.NO_ACTION,
        val weaponType : Int = 0,
        val targetHeroId : Int = 0,
        val result : Int = 0
) : SvpActorSync()