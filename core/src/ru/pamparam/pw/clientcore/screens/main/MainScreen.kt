package ru.pamparam.pw.clientcore.screens.main

import com.badlogic.gdx.Gdx
import ru.pamparam.pw.clientcore.screens.PwMenu
import ru.pamparam.pw.clientcore.screens.PwScreen
import ru.pamparam.pw.packets.ServerPacket

class MainScreen : PwScreen() {
    lateinit var menu : PwMenu
    override fun buildStage() {
        menu = MainMenu()
        stage.addActor(menu.getActor())
        Gdx.input.inputProcessor = stage
    }

    override fun handlePacket(packet: ServerPacket) {
        menu.handlePacket(packet)
    }
}