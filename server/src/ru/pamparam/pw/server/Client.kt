package ru.pamparam.pw.server

import com.badlogic.ashley.core.Entity
import com.pampam.utils.containers.weak
import ru.pamparam.pw.packets.PacketQueue
import ru.pamparam.pw.packets.ServerPacket

class Client(val packetsToQueueToClient: PacketQueue<MessagePacketToClient>,
             val connectionId : Int)
{
    fun SendPacket(packet : ServerPacket) =
            packetsToQueueToClient.push_back(MessagePacketToClient(connectionId, packet))
}

class Account(val accountId : Int)
{
    var client : Client? by weak()
    var activeHero : Hero? by weak()

    val availableHeroIds = mutableListOf<Int>()


}

class Hero(val heroId : Int, val account : Account) {
    var worldEntity : Entity? by weak()

    fun SendPacket(packet : ServerPacket) {
        //println("${heroId} Send packet ${packet.toString()}")
        account.client?.SendPacket(packet)
    }
}