package ru.pamparam.pw.server

import ru.pamparam.pw.packets.ClpActorSync
import ru.pamparam.pw.packets.ClpLogin
import ru.pamparam.pw.packets.PacketQueue
import ru.pamparam.pw.packets.ClientPacket
import ru.pamparam.pw.packets.SvpServerQuantNumber
import ru.pamparam.pw.server.world.WorldManager
import java.time.LocalDateTime


sealed class MessageToGame

data class MessagePacketToServer(val connectionId : Int, val packet : ClientPacket) : MessageToGame()
data class MessageClientDisconnected(val connectionId: Int) : MessageToGame()

data class Position(var x : Float = 0f, var y : Float = 0f, var rotation : Float = 0f)


class QuantumParams() {
    var quantNumber : Int = 0
    var delta : Double = 0.0
    var now : LocalDateTime = LocalDateTime.now()
}

class GameThread(val packetsToQueueToClient: PacketQueue<MessagePacketToClient>,
                 val packetsToQueueToGame: PacketQueue<MessageToGame>) : Thread()
{
    val quantumParams = QuantumParams()

    override fun run() {
        var lastTimeMillis : Long = 0

        var fpsCounter = 0

        while(isAlive) {
            val ms = System.currentTimeMillis()

            if(ms - lastTimeMillis < 16) {
                sleep(8)
                continue
            }

            val secondsToLog = 2
            if((lastTimeMillis / (secondsToLog * 1000.0)).toInt() < (ms / (secondsToLog * 1000.0)).toInt()) {
                println("FPS: ${fpsCounter/secondsToLog} Size: ${WorldManager.worldNode.getMetricsAndFlush()}")
                fpsCounter = 0
            }
            fpsCounter++
            var quantumUpdateTime = ms - lastTimeMillis
            lastTimeMillis = ms

            quantumParams.now = LocalDateTime.now()
            quantumParams.delta = quantumUpdateTime.toDouble()/1000.0 - quantumParams.delta
            quantumParams.quantNumber += 1
            Update();

        }
    }

    fun Update() {
        HeroManager.sendBroadcast(SvpServerQuantNumber(quantumParams.quantNumber))
        do {
            val message = packetsToQueueToGame.pop_front()

            if(message is MessageClientDisconnected) {
                HeroManager.connectionClosed(message.connectionId)
            }

            if(message is MessagePacketToServer) {
                //println("${message.connectionId} packet ${message.packet.toString()}")
                val client = Client(packetsToQueueToClient, message.connectionId)

                when(message.packet) {
                    is ClpLogin -> HeroManager.handlePacket(client, message.packet)
                    is ClpActorSync -> WorldManager.worldNode.handleActorSyncPacket(client, message.packet)
                }
            }
        } while(message != null)

        WorldManager.worldNode.update(quantumParams)
    }
}