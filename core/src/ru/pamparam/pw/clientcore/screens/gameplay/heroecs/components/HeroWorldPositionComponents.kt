package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.esotericsoftware.minlog.Log

enum class RunDest {
    IDLE,
    FRONT,
    FRONT_LEFT,
    FRONT_RIGHT,
    LEFT,
    RIGHT,
    BACK,
    BACK_LEFT,
    BACK_RIGHT
}


class HeroWorldPositionComponent : Component
{
    val body : Body
    var runDestination = RunDest.IDLE
    //var rotation : Float = 0f

    constructor(box2dWorld: World, x : Float, y : Float, rotation : Float, localHero : Boolean, rayHandler: RayHandler) {
        val bodyDef = BodyDef()

        bodyDef.type = when(localHero) {
            true -> BodyDef.BodyType.DynamicBody;
            false -> BodyDef.BodyType.StaticBody
        }
        body = box2dWorld.createBody(bodyDef)
        body.isFixedRotation = true
        val shape = CircleShape()
        shape.radius = 30f
        body.createFixture(shape, 1f);
        body.setTransform(x, y, body.angle)
        shape.dispose();

        if(localHero) {
            val pointLight = PointLight(rayHandler, 128, Color(0f, 0f, 1f, 0.6f), 250f, x, y)
            pointLight.setStaticLight(false);
            pointLight.attachToBody(body)

            val coneLight1 = ConeLight(rayHandler, 128, Color(0f, 1f, 0f, 0.6f), 250f, x, y, 0f, 45f)
            coneLight1.setStaticLight(false);
            coneLight1.attachToBody(body)
        }
    }

    fun toVector2() : Vector2 = Vector2(body.position.x, body.position.y)


}