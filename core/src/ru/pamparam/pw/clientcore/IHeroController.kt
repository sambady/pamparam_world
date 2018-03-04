package ru.pamparam.pw.clientcore

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import ru.pamparam.pw.common.WeaponActionType

abstract class IHeroController(val stage : Stage) {
    data class MoveInfo(val moveRotation: Float, val isMove: Boolean)

    abstract fun getMoveInfo() : MoveInfo
    abstract fun getLookRotation(position : Vector2) : Float
    abstract fun getAndFlushWeaponCommand() : WeaponActionType
    open fun CreateControllerWidget() : Actor? = null
}