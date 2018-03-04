package ru.pamparam.pw.server

import ru.pamparam.pw.packets.PacketQueue


object ServerLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val packetsToQueueToClient = PacketQueue<MessagePacketToClient>()
        val packetsToQueueToServer = PacketQueue<MessageToGame>()
        val networkThread = NetworkThread(packetsToQueueToClient, packetsToQueueToServer)
        val gameThread = GameThread(packetsToQueueToClient, packetsToQueueToServer)

        networkThread.start()
        gameThread.start()

        networkThread.join()
        gameThread.join()
    }
}
