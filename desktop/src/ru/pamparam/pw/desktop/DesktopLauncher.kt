package ru.pamparam.pw.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.clientcore.IPlatformResolver
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen

class PlatformResolverDesktop : IPlatformResolver {
    override fun CreateHeroController(gamePlay: GameplayScreen) : IHeroController =
            KeyboardHeroController(gamePlay)
}

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        PamparamWorld.platformResolver = PlatformResolverDesktop()
        LwjglApplication(PamparamWorld, config)
    }
}
