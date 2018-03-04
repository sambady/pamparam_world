package ru.pamparam.pw.clientcore.screens.gameplay

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Stage

class TiledMap(val stage : Stage) {
    val tiledMapRenderer by lazy {
        val img = Texture("warcraft_summer.png")
        val tiledMap = TmxMapLoader().load("test_map.tmx")
        OrthogonalTiledMapRenderer(tiledMap, stage.batch)
    }

    fun draw(camera: OrthographicCamera) {
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()
    }
}