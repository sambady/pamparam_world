package ru.pamparam.pw.clientcore

import ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components.RunDest

object PositionHelpers
{
    fun angleToRunDestination(angle : Float) = when (angle) {
        in -30..30 -> RunDest.FRONT
        in -60..-30 -> RunDest.FRONT_LEFT
        in 30..60 -> RunDest.FRONT_RIGHT
        in -120..-60 -> RunDest.LEFT
        in 60..120 -> RunDest.RIGHT
        in -150..-120 -> RunDest.BACK_LEFT
        in 120..150 -> RunDest.BACK_RIGHT
        in 150..180 -> RunDest.BACK
        in -180..-150 -> RunDest.BACK
        else -> RunDest.IDLE
    }

    fun speedFromRunDestination(runDestination : RunDest) = when (runDestination) {
        RunDest.FRONT -> 200f
        RunDest.FRONT_LEFT -> 200f
        RunDest.FRONT_RIGHT -> 200f
        RunDest.LEFT -> 200f
        RunDest.RIGHT -> 200f
        RunDest.BACK -> 50f
        RunDest.BACK_LEFT -> 100f
        RunDest.BACK_RIGHT -> 100f
        else -> 0f
    }
}