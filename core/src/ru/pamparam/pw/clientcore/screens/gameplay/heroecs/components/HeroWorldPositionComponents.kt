package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import box2dLight.ConeLight
import box2dLight.DirectionalLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.esotericsoftware.minlog.Log
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen

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


class BodyUserData(val entity: Entity)
{

}

class HeroWorldPositionComponent : Component
{
    val body : Body
    var runDestination = RunDest.IDLE
    //var rotation : Float = 0f

    constructor(gameplayScreen: GameplayScreen, x : Float, y : Float, rotation : Float, localHero : Boolean, entity: Entity) {
        val bodyDef = BodyDef()

        bodyDef.type = when(localHero) {
            true -> BodyDef.BodyType.DynamicBody;
            false -> BodyDef.BodyType.StaticBody
        }
        body = gameplayScreen.box2dWorld.createBody(bodyDef)
        body.isFixedRotation = true
        val shape = CircleShape()
        shape.radius = 1f
        body.createFixture(shape, 1f);
        body.setTransform(x, y, body.angle)
        body.userData = BodyUserData(entity)
        shape.dispose();

        if(localHero) {
            val pointLight = PointLight(gameplayScreen.rayHandler, 128, Color(0f, 0f, 1f, 0.6f), 250f, x/ gameplayScreen.PPM, y/ gameplayScreen.PPM)
            pointLight.setStaticLight(false);
            pointLight.attachToBody(body)
            pointLight.ignoreAttachedBody = true
            pointLight.isSoft = true

            val coneLight1 = ConeLight(gameplayScreen.rayHandler, 128, Color(0f, 1f, 0f, 0.6f), 250f, x/ gameplayScreen.PPM, y/ gameplayScreen.PPM, 0f, 45f)
            coneLight1.setStaticLight(false);
            coneLight1.attachToBody(body)
            coneLight1.ignoreAttachedBody = true
            coneLight1.isXray = true
            coneLight1.isSoft = true
            coneLight1.setContactFilter(0x0004, 0, -1)
        }
    }

    fun toVector2() : Vector2 = Vector2(body.position.x , body.position.y)


}