package ru.pamparam.pw.clientcore.screens

import com.badlogic.gdx.scenes.scene2d.Actor
import ru.pamparam.pw.packets.ServerPacket

interface PwMenu {
    fun handlePacket(packet : ServerPacket)
    fun getActor() : Actor
}