package ru.pamparam.pw.clientcore

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage

interface IPlatformResolver {
    fun CreateHeroController(camera: OrthographicCamera) : IHeroController
}