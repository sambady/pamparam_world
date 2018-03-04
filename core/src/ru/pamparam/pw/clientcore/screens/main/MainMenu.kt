package ru.pamparam.pw.clientcore.screens.main

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import ktx.actors.onClick
import ktx.scene2d.KButton
import ktx.scene2d.button
import ktx.scene2d.label
import ktx.scene2d.table
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.Network
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.PwMenu
import ru.pamparam.pw.packets.ClpCreateNewAccountHeroAndSelect
import ru.pamparam.pw.packets.ServerPacket
import ru.pamparam.pw.packets.SvpCreateNewAccountAndHeroComplete


class MainMenu : PwMenu {
    private val actor = table {
        setFillParent(true)
        pad(50f)

        button {
            label("START GAME")
            onClick { event: InputEvent, actor: KButton ->
                StartGame()
            }
        }
    }.debug()

    override fun getActor(): Actor = actor

    override fun handlePacket(packet: ServerPacket) {
        when(packet) {
            is SvpCreateNewAccountAndHeroComplete -> onSvpCreateNewAccountAndHeroComplete(packet)
        }
    }

    private fun StartGame() {
        PamparamWorld.network.sendPacket(ClpCreateNewAccountHeroAndSelect())
    }
    private fun onSvpCreateNewAccountAndHeroComplete(packet : SvpCreateNewAccountAndHeroComplete) {
        println("onSvpCreateNewAccountAndHeroComplete")
        PamparamWorld.screenManager.setNewScreen(GameplayScreen())
    }

}