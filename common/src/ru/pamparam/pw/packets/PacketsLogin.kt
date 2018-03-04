package ru.pamparam.pw.packets

import com.esotericsoftware.kryo.Kryo
import kotlin.reflect.KClass


sealed class ClpLogin : ClientPacket()
sealed class SvpLogin : ServerPacket()

fun RegisterLoginPackets(prefix : Int, kryo : Kryo) {
    var idx = 1
    fun <T : Any>registerPacket(packet : KClass<T>) {
        println("RegisterLoginPackets ${packet.toString()} ${prefix + idx}")
        kryo.register(packet.java, prefix + idx)
        idx += 1
    }
    registerPacket(ClpCreateNewAccount::class)
    registerPacket(SvpCreateNewAccount::class)
    registerPacket(ClpLoginAndGetHeroList::class)
    registerPacket(SvpHeroList::class)
    registerPacket(ClpSelectHero::class)
    registerPacket(ClpCreateHeroAndSelect::class)
    registerPacket(ClpCreateNewAccountHeroAndSelect::class)
    registerPacket(SvpCreateNewAccountAndHeroComplete::class)
}

class ClpCreateNewAccount : ClpLogin()

data class SvpCreateNewAccount(val accountId: Int = 0) : SvpLogin()

data class ClpLoginAndGetHeroList(val accountId : Int = 0) : ClpLogin()

data class SvpHeroList(val heroList : List<Int> = listOf()) : SvpLogin()

data class ClpSelectHero(val heroId : Int = 0) : ClpLogin()

data class ClpCreateHeroAndSelect(val name : String = "") : ClpLogin()

data class ClpCreateNewAccountHeroAndSelect(val requestId : Int = 0) : ClpLogin()
data class SvpCreateNewAccountAndHeroComplete(val requestId: Int = 0) : SvpLogin()
