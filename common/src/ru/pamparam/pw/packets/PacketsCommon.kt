package ru.pamparam.pw.packets

import com.esotericsoftware.kryo.Kryo
import ru.pamparam.pw.common.WeaponActionType


open class ClientPacket
open class ServerPacket

fun RegisterAllPackets(kryo: Kryo) {
    kryo.register(ArrayList::class.java, 55555)
    kryo.register(WeaponActionType::class.java, 55556)
    RegisterLoginPackets(0, kryo)
    RegisterActorSyncPackets(100, kryo)
    RegisterServicePackets(200, kryo)
}