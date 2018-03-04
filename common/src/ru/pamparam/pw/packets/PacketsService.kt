package ru.pamparam.pw.packets

import com.esotericsoftware.kryo.Kryo
import kotlin.reflect.KClass


sealed class SvpServerService : ServerPacket()

fun RegisterServicePackets(prefix : Int, kryo : Kryo) {
    var idx = 1
    fun <T : Any>registerPacket(packet : KClass<T>) {
        println("RegisterServicePackets ${packet.toString()} ${prefix + idx}")
        kryo.register(packet.java, prefix + idx)
        idx += 1
    }
    registerPacket(SvpServerQuantNumber::class)
}

data class SvpServerQuantNumber(val serverQuant : Int = 0) : SvpServerService()