package ru.pamparam.pw.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.scenes.scene2d.Stage
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.clientcore.IPlatformResolver
import ru.pamparam.pw.clientcore.PamparamWorld

class PlatformResolverDesktop : IPlatformResolver {
    override fun CreateHeroController(stage : Stage) : IHeroController = KeyboardHeroController(stage)
}

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        PamparamWorld.platformResolver = PlatformResolverDesktop()
        LwjglApplication(PamparamWorld, config)
    }
}
