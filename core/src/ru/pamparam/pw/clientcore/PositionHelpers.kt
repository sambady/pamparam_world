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

    fun speedFromRunDestination(runDestination : RunDest) : Float {
        var baseSpeed = 2f
        return baseSpeed * when (runDestination) {
            RunDest.FRONT -> 3f
            RunDest.FRONT_LEFT -> 3f
            RunDest.FRONT_RIGHT -> 3f
            RunDest.LEFT -> 2f
            RunDest.RIGHT -> 2f
            RunDest.BACK -> 1f
            RunDest.BACK_LEFT -> 1.5f
            RunDest.BACK_RIGHT -> 1.5f
            else -> 0f
        }
    }
}