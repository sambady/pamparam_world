package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import box2dLight.ConeLight
import box2dLight.PointLight
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen

class BulletComponent : Component
{
    val texture by lazy { Texture("bullet.png") }
    val body : Body
    var lifetime = 3f
    val gameplay : GameplayScreen
    val pointLight : PointLight
    constructor(gameplay_: GameplayScreen, position : Vector2, rotation : Float) {
        gameplay = gameplay_
        val bodyDef = BodyDef()

        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(-texture.width.toFloat()/2f, -texture.height.toFloat()/2f)


        body = gameplay.box2dWorld.createBody(bodyDef)
        body.massData.mass = 0.001f
        body.isFixedRotation = true
        val shape = CircleShape()
        shape.radius = 2f

        body.createFixture(shape, 1f);
        body.setTransform(position.x, position.y, (rotation + 90f)* MathUtils.degreesToRadians)

        val speed = Vector2(10000000000000f, 0f).rotate(rotation)

        Log.info("Dir ${speed}")
        body.applyForceToCenter(speed, true)

        pointLight = PointLight(gameplay.rayHandler, 128, Color(1f, 0f, 0f, 1.0f), 10f, 0f, 0f)
        pointLight.setStaticLight(false);
        pointLight.attachToBody(body)



        shape.dispose();
    }

    fun dispose() {

        gameplay.box2dWorld.destroyBody(body)
        pointLight.remove(true)

        texture.dispose()
    }
}