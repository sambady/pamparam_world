package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2

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


class HeroWorldPositionComponent(var x : Float = 0f, var y : Float = 0f, var rotation : Float = 0f) : Component
{
    var runDestination = RunDest.IDLE
    fun toVector2() : Vector2 = Vector2(x, y)
}
