package ru.pamparam.pw.clientcore

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.esotericsoftware.minlog.Log
import ktx.scene2d.Scene2DSkin
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.clientcore.screens.ScreenManager
import ru.pamparam.pw.packets.*

fun normolizeAngle(angle : Float) : Float {
    var newAngle = angle
    while (newAngle <= -180) newAngle += 360
    while (newAngle > 180) newAngle -= 360
    return newAngle
}

class Network {
    private val packetsFromServer = PacketQueue<ServerPacket>()
    private val network = ru.pamparam.pw.clientcore.NetworkThread(packetsFromServer)

    fun start() {
        network.start()
    }

    fun handlePackets(visitor : (ServerPacket) -> Unit) {
        do {
            val packet = packetsFromServer.pop_front()
            if(packet == null)
                break
            visitor(packet)
        }while(true)
    }

    fun sendPacket(packet : ClientPacket) {
        network.sendPacket(packet)
    }

    fun dispose() {
        network.dispose()
    }

    fun getStatsAndFlush() : Int = network.getStatsAndFlush()
}

object PamparamWorld : ApplicationAdapter() {
    lateinit var platformResolver : IPlatformResolver

    val screenManager by lazy {
        ScreenManager()
    }

    val network by lazy {
        Network()
    }
    
    override fun create() {
        Log.info("Start create")
        // TODO init UI skins
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("skin/neon-ui.json"))

        screenManager.create()
        network.start()

        Gdx.app.getPreferences("sync").putInteger("quant_delay", 4)

        Log.info("End create")
    }

    override fun resize(width: Int, height: Int) {
        Log.debug("Resize ${width}:${height}")
        screenManager.resize(width, height)
    }

    override fun render() {
        network.handlePackets {
            if (it is ServerPacket) {
                screenManager.currentScreen.handlePacket(it)
            }
        }
        screenManager.render(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        network.dispose()
        screenManager.dispose()
    }

    fun sendPacket(packet : ClientPacket) {
        network.sendPacket(packet)
    }

    fun setDebug1(message : String) {
        val currentScreen = screenManager.currentScreen
        if(currentScreen is GameplayScreen) {
            currentScreen.debugLabel1.text = message
        }
    }

    fun setDebug2(message : String) {
        val currentScreen = screenManager.currentScreen
        if(currentScreen is GameplayScreen) {
            currentScreen.debugLabel2.text = message
        }
    }
}

typealias Pw = PamparamWorld