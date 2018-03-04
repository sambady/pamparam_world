package ru.pamparam.pw.clientcore.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ru.pamparam.pw.packets.ServerPacket

abstract class PwScreen : Screen {
    val spriteBatch by lazy {
        SpriteBatch()
    }
    val stage by lazy {
        val camera = OrthographicCamera()
        //camera.setToOrtho(false, 640f, 480f)
        camera.setToOrtho(false)
        val viewport = FitViewport(640f, 480f, camera)
        val st = Stage (viewport, spriteBatch)
        st.setDebugAll(true)
        st
    }

    abstract fun buildStage()
    abstract fun handlePacket(packet : ServerPacket)

    override fun show() {
        //Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0f, 0f,0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calling to Stage methods
        stage.batch.begin()
        stage.act(delta)
        stage.batch.end()
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    override fun hide() {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        spriteBatch.dispose()
    }

}