package ru.pamparam.pw.packets

import java.util.*

class PacketQueue<T> {
    private val packetQueue : Queue<T> = LinkedList<T>()

    @Synchronized
    fun push_back(packet : T) {
        packetQueue.add(packet)
        add++
    }

    @Synchronized
    fun pop_front() : T? {
        if(packetQueue.isEmpty())
            return null
        get++
        return packetQueue.remove()
    }

    val size
        @Synchronized get() = packetQueue.size

    private var add = 0
    private var get = 0
    fun getMetricsAndFlush() : String {
        val str = "Add ${add} Get ${add} Size: ${size}"
        add = 0
        get = 0
        return str
    }

}