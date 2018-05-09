package ru.pamparam.pw.clientcore

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.packets.ClientPacket
import ru.pamparam.pw.packets.PacketQueue
import ru.pamparam.pw.packets.RegisterAllPackets
import ru.pamparam.pw.packets.ServerPacket
import java.io.IOException

class ServerPacketHandler(val packetsQueueToClient: PacketQueue<ServerPacket>) : Listener() {
    override fun received(connection : Connection, packet : Any) {
        if(packet !is ServerPacket)
            return
        packetsQueueToClient.push_back(packet)
    }
}

class NetworkThread(val packetsQueueToClient: PacketQueue<ServerPacket>) : Thread() {
    private val client = Client()
    private val packetsQueueToServer = PacketQueue<ClientPacket>()
    private var sended = 0

    override fun run() {
        RegisterAllPackets(client.kryo)

        client.start()
        client.addListener(ru.pamparam.pw.clientcore.ServerPacketHandler(packetsQueueToClient))
        var connected = false

        while(isAlive) {

            if(!connected) {
                try {
                    client.connect(5000, "127.0.0.1", 5756)
                    Log.info("Connected")
                    connected = true
                }
                catch (ioException : IOException) {
                    Log.info("Error: ${ioException.message}");
                    sleep(1000)
                    continue
                }
            }
            if(!client.isConnected) {
                connected = false
                continue
            }

            val packet = packetsQueueToServer.pop_front()
            if(packet == null) {
                sleep(1)
                continue
            }

            sended += client.sendTCP(packet)
        }
    }

    fun sendPacket(packet : ClientPacket) {
        packetsQueueToServer.push_back(packet)
    }

    fun dispose() {
        interrupt()
        join()
    }

    fun getStatsAndFlush() : Int {
        val i = sended
        sended = 0
        return i
    }

}
