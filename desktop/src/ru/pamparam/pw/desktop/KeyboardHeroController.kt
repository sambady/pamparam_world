package ru.pamparam.pw.desktop

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.clientcore.normolizeAngle
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.common.WeaponActionType

class KeyboardHeroController(gamePlay : GameplayScreen) : IHeroController(gamePlay), InputProcessor {
    private var pressedW = false
    private var pressedS = false
    private var pressedA = false
    private var pressedD = false
    private var mouseDirection = Vector2()

    private var weaponCommand : WeaponActionType = WeaponActionType.none

    override fun getMoveInfo() : IHeroController.MoveInfo {
        var xy = Vector2()
        if(pressedW) xy.y += 1
        if(pressedS) xy.y -= 1
        if(pressedA) xy.x -= 1
        if(pressedD) xy.x += 1

        if(xy.x == 0f && xy.y == 0f) {
            return ru.pamparam.pw.clientcore.IHeroController.MoveInfo(0f, false)
        }

        var positionDelta = when(xy) {
            Vector2(1f, 1f) -> Vector2(1/Math.sqrt(2.0).toFloat(), 1/Math.sqrt(2.0).toFloat())
            Vector2(1f, -1f) -> Vector2(1/Math.sqrt(2.0).toFloat(), -1/Math.sqrt(2.0).toFloat())
            Vector2(-1f, 1f) -> Vector2(-1/Math.sqrt(2.0).toFloat(), 1/Math.sqrt(2.0).toFloat())
            Vector2(-1f, -1f) -> Vector2(-1/Math.sqrt(2.0).toFloat(), -1/Math.sqrt(2.0).toFloat())
            else -> xy
        }

        return ru.pamparam.pw.clientcore.IHeroController.MoveInfo(positionDelta.angle(), true)
    }

    override fun getAndFlushWeaponCommand(): WeaponActionType {
        val command = weaponCommand
        weaponCommand = WeaponActionType.none
        return command
    }


    override fun getLookRotation(position : Vector2) : Float {

        val mouseWorldPosition = gamePlay.worldCamera.unproject(Vector3(mouseDirection.x, mouseDirection.y, 0f))
/*
        val tmp = Vector3()
        tmp.set(screenCoords.x, screenCoords.y, 1f)
        camera.unproject(tmp, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        val c = camera as OrthographicCamera
        c.view
        val mouseWorldPosition = stage.screenToStageCoordinates(Vector2(mouseDirection))
val st = Stage()
        st.screenToStageCoordinates()*/
        var degrees = Math.atan2(
                (mouseWorldPosition.y.toDouble() - position.y.toDouble()),
                (mouseWorldPosition.x.toDouble() - position.x.toDouble())) * MathUtils.radiansToDegrees

        return normolizeAngle(degrees.toFloat())
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        mouseDirection = Vector2(screenX.toFloat(), screenY.toFloat())
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        when(keycode) {
            Input.Keys.W -> pressedW = false
            Input.Keys.S -> pressedS = false
            Input.Keys.A -> pressedA = false
            Input.Keys.D -> pressedD = false
        }
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        when(keycode) {
            Input.Keys.W -> pressedW = true
            Input.Keys.S -> pressedS = true
            Input.Keys.A -> pressedA = true
            Input.Keys.D -> pressedD = true
            Input.Keys.NUM_1 -> weaponCommand = WeaponActionType.selectKnife
            Input.Keys.NUM_2 -> weaponCommand = WeaponActionType.selectPistol
            Input.Keys.NUM_3 -> weaponCommand = WeaponActionType.selectRifle
            Input.Keys.R -> weaponCommand = WeaponActionType.reload
        }
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when(button) {
            Input.Buttons.LEFT -> weaponCommand = WeaponActionType.primaryAttack
            Input.Buttons.RIGHT -> weaponCommand = WeaponActionType.secondaryAttack
        }
        return true
    }
}