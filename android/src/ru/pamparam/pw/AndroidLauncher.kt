package ru.pamparam.pw

import android.os.Bundle

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.scenes.scene2d.Stage
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.clientcore.IPlatformResolver
import ru.pamparam.pw.clientcore.PamparamWorld

class PlatformResolverAndroid : IPlatformResolver {
    override fun CreateHeroController(stage : Stage): IHeroController = TouchHeroController(stage)
}


class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        PamparamWorld.platformResolver = PlatformResolverAndroid()
        initialize(PamparamWorld, config)
    }
}
