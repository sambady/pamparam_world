package ru.pamparam.pw.clientcore.screens.gameplay

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import ru.pamparam.pw.packets.ServerPacket
import ru.pamparam.pw.packets.SvpActorSync
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.ui.TextArea
import com.badlogic.gdx.utils.Align
import com.esotericsoftware.minlog.Log
import ktx.scene2d.*
import ru.pamparam.pw.clientcore.Pw
import ru.pamparam.pw.clientcore.screens.PwScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.HeroEcs
import ru.pamparam.pw.packets.SvpLocalHeroInit
import ru.pamparam.pw.packets.SvpServerQuantNumber


class Bullet(val position : Vector2, val direction : Vector2, var lifeTime : Float)
{
    init {
        Log.info("PositionBullet ${position}")
    }

    val texture by lazy {
        Texture("bullet.png")
    }
    fun UpdateAndDraw(delta : Float, batch: SpriteBatch) {
        if(lifeTime > 0f) {
            position.x += direction.x
            position.y += direction.y
            //Log.info("Draw ${position}")
            val aff = Affine2()
            aff.translate(position)
            aff.rotate(direction.angle() + 90f)
            val textureRegion = TextureRegion(texture)

            batch.draw(textureRegion, texture.width.toFloat(), texture.height.toFloat(), aff)
            lifeTime -= delta
        }
    }
}

class GameplayScreen : PwScreen() {
    val heroEcs : HeroEcs
    val localHeroEcs : HeroEcs
    val box2dWorld : World
    val rayHandler : RayHandler
    val tiledMap : TiledMap
    val worldCamera : OrthographicCamera
    val worldSpriteBatch : SpriteBatch

    init {
        worldCamera = OrthographicCamera()
        worldSpriteBatch = SpriteBatch()
        box2dWorld = World(Vector2(0f, 0f), false)
        rayHandler = RayHandler(box2dWorld)


        heroEcs = HeroEcs(this, worldCamera)
        localHeroEcs = HeroEcs(this, worldCamera)
        tiledMap = TiledMap(this, worldSpriteBatch)

        rayHandler.setAmbientLight(0f, 0f, 0f, 0.3f);
    }

    var serverQuantNumber = 0
        private set
    var clientQuantNumber = 0
        private set

    val debugLabel1 = TextArea("TODO", Scene2DSkin.defaultSkin)
    val debugLabel2 = TextArea("TODO2", Scene2DSkin.defaultSkin)

    override fun buildStage() {
        debugLabel1.setAlignment(Align.left)
        debugLabel2.setAlignment(Align.right)
        stage.addActor(table {
            setFillParent(true)

            add(debugLabel1).left().width(500f).expandX()
            add(debugLabel2).expandX().right()

        }.align(Align.top))

        heroEcs.localHeroController.CreateControllerWidget()?.let {
            stage.addActor(it)
        }

        val inputMultiplexer = InputMultiplexer()
        inputMultiplexer.addProcessor(heroEcs.localHeroController as InputProcessor)
        inputMultiplexer.addProcessor(stage)
        Gdx.input.inputProcessor = inputMultiplexer

        worldCamera.setToOrtho(false, 640f, 320f)

        heroEcs.handleSvpActorSync(SvpLocalHeroInit(0, 320f, 240f))
    }

    override fun handlePacket(packet: ServerPacket) {
        when(packet) {
            is SvpServerQuantNumber -> serverQuantNumber = packet.serverQuant
            is SvpLocalHeroInit -> localHeroEcs.handleSvpActorSync(packet)
            is SvpActorSync -> heroEcs.handleSvpActorSync(packet)
        }
    }

    var bullet : Bullet? = null

    val box2dDebugRenderer = Box2DDebugRenderer(true, true, true, true, true, true)
    override fun render(delta: Float) {
        clientQuantNumber++
        Gdx.gl.glClearColor(0f, 0f,0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        val heroPosition = heroEcs.getLocalHeroPosition()
        worldCamera.position.set(heroPosition, 0f)

        worldCamera.update()
        tiledMap.draw(worldCamera)

        worldSpriteBatch.projectionMatrix = worldCamera.combined

        worldSpriteBatch.begin()
        heroEcs.update(delta)


        worldSpriteBatch.end()
        box2dWorld.step(delta, 1, 1)
        rayHandler.setCombinedMatrix(worldCamera)
        rayHandler.updateAndRender();



        Pw.setDebug2("${Pw.network.getStatsAndFlush()}")
        stage.act(delta)
        stage.draw()



        worldSpriteBatch.begin()
        localHeroEcs.update(delta)

        bullet?.let {
            //Log.info("Position: ${it.position}")
            it.UpdateAndDraw(delta, worldSpriteBatch)
            if(it.lifeTime < 0f) {
                bullet = null
            }
        }

        worldSpriteBatch.end()


        val debugMatrix = Matrix4(worldCamera.combined)
        //box2dDebugRenderer.render(box2dWorld, debugMatrix)


    }
}