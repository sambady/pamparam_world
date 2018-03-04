package ru.pamparam.pw.server.world.ecscomponents

import com.badlogic.ashley.core.Component

class PositionComponent(var x : Float = 0f,
                        var y : Float = 0f,
                        var rotation : Float = 0f,
                        var isMove : Boolean = false) : Component
{

}