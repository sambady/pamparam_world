package ru.pamparam.pw.clientcore

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.common.WeaponActionType

abstract class IHeroController(val gamePlay: GameplayScreen) {
    data class MoveInfo(val moveRotation: Float, val isMove: Boolean)

    abstract fun getMoveInfo() : MoveInfo
    abstract fun getLookRotation(position : Vector2) : Float
    abstract fun getAndFlushWeaponCommand() : WeaponActionType
    open fun CreateControllerWidget() : Actor? = null
}