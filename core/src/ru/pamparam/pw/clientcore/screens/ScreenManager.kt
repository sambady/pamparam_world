package ru.pamparam.pw.clientcore.screens

import ru.pamparam.pw.clientcore.screens.main.MainScreen


class ScreenManager {
    lateinit var currentScreen : PwScreen
        private set

    fun create() {
        currentScreen = MainScreen()
        currentScreen.buildStage()
        currentScreen.show()
    }

    fun resize(width : Int, height : Int) {
        currentScreen.resize(width, height)
    }
    fun pause() {
        currentScreen.pause()
    }
    fun render(delta : Float) {
        currentScreen.render(delta)
    }

    fun setNewScreen(newScreen : PwScreen) {
        val oldScreen = currentScreen
        newScreen.buildStage()
        currentScreen = newScreen
        currentScreen.show()
        oldScreen.dispose()
    }

    fun dispose() {
        currentScreen.dispose()
    }
}