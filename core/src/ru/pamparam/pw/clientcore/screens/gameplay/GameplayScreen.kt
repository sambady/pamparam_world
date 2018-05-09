package ru.pamparam.pw.clientcore.screens.gameplay

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Entity
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
import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.esotericsoftware.minlog.Log
import ktx.scene2d.*
import ru.pamparam.pw.clientcore.Pw
import ru.pamparam.pw.clientcore.screens.PwScreen
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.HeroEcs
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.BodyUserData
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.BulletComponent
import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.BulletUserData
import ru.pamparam.pw.packets.SvpLocalHeroInit
import ru.pamparam.pw.packets.SvpServerQuantNumber

class GameplayScreen : PwScreen() {
    val heroEcs : HeroEcs
    val box2dWorld : World
    val rayHandler : RayHandler
    val tiledMap : TiledMap
    val worldCamera : OrthographicCamera
    val worldSpriteBatch : SpriteBatch
    //val worldViewport : FitViewport
    val PPM = 20f

    var bulletForDelete = mutableListOf<Entity>()

    init {
        worldCamera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        worldSpriteBatch = SpriteBatch()

        World.setVelocityThreshold(10000f)
        box2dWorld = World(Vector2(0f, 0f), false)


        rayHandler = RayHandler(box2dWorld)

        heroEcs = HeroEcs(this, worldCamera)
        tiledMap = TiledMap(this, worldSpriteBatch)

        rayHandler.setAmbientLight(0f, 0f, 0f, 0.3f);

        box2dWorld.setContactListener(object : ContactListener {
            override fun endContact(contact: Contact?) {

            }

            override fun beginContact(contact: Contact?) {
                if(contact == null)
                    return
                val userData = contact.fixtureA.body.userData
                if( userData is BulletUserData) {
                    bulletForDelete.add(userData.entity)

                    val userDataHero = contact.fixtureB.body.userData
                    if(userDataHero is BodyUserData) {
                        Log.info("BOO")
                    }
                }
                val userData2 = contact.fixtureB?.body?.userData
                if( userData2 is BulletUserData) {
                    bulletForDelete.add(userData2.entity)

                    val userDataHero = contact.fixtureA.body.userData
                    if(userDataHero is BodyUserData) {
                        Log.info("BOO")
                    }
                }
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {

            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {

            }
        })
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



        worldCamera.setToOrtho(false, Gdx.graphics.width/1.5f/PPM, Gdx.graphics.height/1.5f/PPM)

        heroEcs.handleSvpActorSync(SvpLocalHeroInit(0, Gdx.graphics.width/PPM/2f, Gdx.graphics.height/PPM/2f))

    }

    override fun handlePacket(packet: ServerPacket) {
        when(packet) {
            is SvpServerQuantNumber -> serverQuantNumber = packet.serverQuant
            is SvpLocalHeroInit -> heroEcs.handleSvpActorSync(packet)
            is SvpActorSync -> heroEcs.handleSvpActorSync(packet)
        }
    }

    val box2dDebugRenderer = Box2DDebugRenderer(true, true, true, true, true, true)
    override fun render(delta: Float) {
        clientQuantNumber++
        Gdx.gl.glClearColor(0f, 0f,0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        val heroPosition = heroEcs.getLocalHeroPosition()
        worldCamera.position.set(heroPosition, 0f)
        worldCamera.update()

        worldSpriteBatch.projectionMatrix = worldCamera.combined
        tiledMap.draw(worldCamera, worldSpriteBatch)

        heroEcs.update(delta)
        worldSpriteBatch.begin()
        heroEcs.draw(worldSpriteBatch)
        worldSpriteBatch.end()
        box2dWorld.step(1f/60f, 1, 1)
        rayHandler.setCombinedMatrix(worldCamera)
        rayHandler.updateAndRender()

        Pw.setDebug2("${heroPosition?.x} : ${heroPosition.y}")
        stage.act(delta)
        stage.draw()

        for(ent in bulletForDelete) {
            ent.getComponent(BulletComponent::class.java)?.dispose()
            heroEcs.engine.removeEntity(ent)
        }
        bulletForDelete.clear()

        val debugMatrix = Matrix4(worldCamera.combined)
        //box2dDebugRenderer.render(box2dWorld, debugMatrix)


    }
}