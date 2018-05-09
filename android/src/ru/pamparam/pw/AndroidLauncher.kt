package ru.pamparam.pw

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.clientcore.IPlatformResolver
import ru.pamparam.pw.clientcore.PamparamWorld
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen

class PlatformResolverAndroid : IPlatformResolver {
    override fun CreateHeroController(gamePlay: GameplayScreen): IHeroController
            = TouchHeroController(gamePlay)
}


class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        PamparamWorld.platformResolver = PlatformResolverAndroid()
        initialize(PamparamWorld, config)
    }
}
