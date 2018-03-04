package ru.pamparam.pw

import com.badlogic.gdx.backends.iosmoe.IOSApplication
import com.badlogic.gdx.backends.iosmoe.IOSApplicationConfiguration
import org.moe.natj.general.Pointer
import apple.uikit.c.UIKit
import ru.pamparam.pw.client_core.PamparamWorld

class IOSMoeLauncher protected constructor(peer: Pointer) : IOSApplication.Delegate(peer) {

    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration()
        config.useAccelerometer = false
        return IOSApplication(PamparamWorld, config)
    }

    companion object {

        @JvmStatic
        fun main(argv: Array<String>) {
            UIKit.UIApplicationMain(0, null, null, IOSMoeLauncher::class.java.name)
        }
    }
}
