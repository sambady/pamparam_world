package ru.pamparam.pw.clientcore.screens.gameplay

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.HeroEcs
import ru.pamparam.pw.clientcore.screens.PwScreen
import ru.pamparam.pw.packets.ServerPacket
import ru.pamparam.pw.packets.SvpActorSync
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.TextArea
import com.badlogic.gdx.utils.Align
import com.esotericsoftware.minlog.Log
import ktx.scene2d.*
import ru.pamparam.pw.clientcore.NetworkThread
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.Pw
import ru.pamparam.pw.packets.SvpLocalHeroInit
import ru.pamparam.pw.packets.SvpServerQuantNumber


class GameplayScreen : PwScreen() {
    val heroEcs : HeroEcs
    val tiledMap : TiledMap
    val worldCamera : OrthographicCamera
    val worldSpriteBatch : SpriteBatch

    init {
        worldCamera = OrthographicCamera()
        worldSpriteBatch = SpriteBatch()

        heroEcs = HeroEcs(this, worldCamera)
        tiledMap = TiledMap(worldSpriteBatch)
    }

    var serverQuantNumber = 0
        private set
    var clientQuantNumber = 0
        private set

    val debugLabel1 = TextArea("TODO", Scene2DSkin.defaultSkin)
    val debugLabel2 = TextArea("TODO2", Scene2DSkin.defaultSkin)

    override fun buildStage() {
        debugLabel1.setAlignment(Align.left)
        debugLabel2.setAlignment(Align.right)
        stage.addActor(table {
            setFillParent(true)

            add(debugLabel1).left().width(500f).expandX()
            add(debugLabel2).expandX().right()

        }.align(Align.top))

        heroEcs.localHeroController.CreateControllerWidget()?.let {
            stage.addActor(it)
        }

        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(heroEcs.localHeroController as InputProcessor)
        inputMultiplexer.addProcessor(stage)
        Gdx.input.inputProcessor = inputMultiplexer

        worldCamera.setToOrtho(false, 640f, 320f)

        heroEcs.handleSvpActorSync(SvpLocalHeroInit(0, 320f, 240f))
    }

    override fun handlePacket(packet: ServerPacket) {
        when(packet) {
            is SvpServerQuantNumber -> serverQuantNumber = packet.serverQuant
            is SvpActorSync -> heroEcs.handleSvpActorSync(packet)
        }
    }

    override fun render(delta: Float) {
        clientQuantNumber++
        Gdx.gl.glClearColor(0f, 0f,0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        val heroPosition = heroEcs.getLocalHeroPosition()
        worldCamera.position.set(heroPosition, 0f)

        worldCamera.update()
        tiledMap.draw(worldCamera)
        worldSpriteBatch.projectionMatrix = worldCamera.combined

        worldSpriteBatch.begin()
        heroEcs.update(delta)
        worldSpriteBatch.end()
        PamparamWorld.setDebug2("${Pw.network.getStatsAndFlush()}")
        stage.act(delta)
        stage.draw()
    }
}