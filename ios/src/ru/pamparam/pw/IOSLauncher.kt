package ru.pamparam.pw

import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import ru.pamparam.pw.clientcore.PamparamWorld


class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration()
        return IOSApplication(PamparamWorld, config)
    }

    companion object {

        @JvmStatic
        fun main(argv: Array<String>) {
            val pool = NSAutoreleasePool()
            UIApplication.main<UIApplication, IOSLauncher>(argv, null, IOSLauncher::class.java)
            pool.close()
        }
    }
}