package ru.pamparam.pw.server

import ru.pamparam.pw.packets.ClientPacket

class ClientManager
{
    val clientsByConnectionId = mutableMapOf<Int, Client>()
    val clientsByAccountId = mutableMapOf<Int, Client>()
    val clientsByHeroId = mutableMapOf<Int, Client>()


    fun HandlePacket(packet : ClientPacket) {

    }


}