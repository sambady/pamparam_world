package ru.pamparam.pw.server

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import ru.pamparam.pw.packets.ClientPacket
import ru.pamparam.pw.packets.ServerPacket
import ru.pamparam.pw.packets.PacketQueue
import ru.pamparam.pw.packets.RegisterAllPackets


data class MessagePacketToClient(val connectionId : Int, val packet : ServerPacket)

class ClientSessionManager(val packetsToQueueToGame: PacketQueue<MessageToGame>) : Listener() {

    override fun connected(connection: Connection?) {
        if(connection == null)
            return
        //packetsToQueueToGame.push_back(MessageClientConnected(connection.id, 1, 1))
    }

    override fun received(connection : Connection, packet : Any) {
        if(packet !is ClientPacket)
            return

        packetsToQueueToGame.push_back(MessagePacketToServer(connection.id, packet))
    }

    override fun disconnected(connection: Connection) {
        packetsToQueueToGame.push_back(MessageClientDisconnected(connection.id))
    }
}

class NetworkThread(val packetsToQueueToClient: PacketQueue<MessagePacketToClient>,
                    val packetsToQueueToGame: PacketQueue<MessageToGame>) : Thread() {
    private val clientSessions = ClientSessionManager(packetsToQueueToGame)

    private val server = Server()

    override fun run() {
        RegisterAllPackets(server.kryo)
        server.start()
        server.bind(57569)
        server.addListener(clientSessions)

        while(isAlive) {
            val message = packetsToQueueToClient.pop_front()
            if(message == null) {
                sleep(1)
                continue
            }

            server.sendToTCP(message.connectionId, message.packet)
        }
    }

    fun send_packet(clientConnectionId: Int, packet : ServerPacket) {
        packetsToQueueToClient.push_back(MessagePacketToClient(clientConnectionId, packet))
    }
}


