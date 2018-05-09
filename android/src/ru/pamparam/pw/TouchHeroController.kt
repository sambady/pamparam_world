package ru.pamparam.pw

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.Align
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.table
import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen
import ru.pamparam.pw.common.WeaponActionType
import java.lang.Math.abs

class TouchHeroController(gamePlay: GameplayScreen)
    : ru.pamparam.pw.clientcore.IHeroController(gamePlay), InputProcessor by gamePlay.stage
{
    class StickerPosition(val x : Int = 0, val y : Int = 0)
    private var leftSticker : StickerPosition? = null
    private var rightSticker : StickerPosition? = null

    var moveRotation = 0f
    //var viewRotation = 0f
    //var isMove = false

    var rightStickIsTouched = false

    var lastRotation = 0f
    private var weaponCommand : WeaponActionType? = null
    override fun getMoveInfo(): ru.pamparam.pw.clientcore.IHeroController.MoveInfo {

        val isMove = abs(leftStickWidget.knobPercentX) != 0f ||
                abs(leftStickWidget.knobPercentY) != 0f

        val rotation = Vector2(leftStickWidget.knobPercentX, leftStickWidget.knobPercentY).nor().angle()
        if(isMove)
            lastRotation = rotation
        return ru.pamparam.pw.clientcore.IHeroController.MoveInfo(rotation, isMove)
    }

    override fun getLookRotation(position: Vector2): Float {
        if(rightStickWidget.knobPercentX != 0f && rightStickWidget.knobPercentY != 0f)
            lastRotation = Vector2(rightStickWidget.knobPercentX, rightStickWidget.knobPercentY).nor().angle()

        return lastRotation
    }
        
    override fun getAndFlushWeaponCommand(): WeaponActionType {
        if(rightStickIsTouched && !rightStickWidget.isTouched) {
            rightStickIsTouched = false
            return WeaponActionType.primaryAttack
        }
        if(!rightStickIsTouched && rightStickWidget.isTouched) {
            rightStickIsTouched = true
        }
        return WeaponActionType.none
    }
/*
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return true
    }

    override fun scrolled(amount: Int): Boolean {
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        /*val ls = leftSticker
        if(ls == null)
            return true;



        val len = Math.abs((screenX - ls.x) * (screenX - ls.x) - (screenY - ls.y) * (screenY - ls.y))
        val v : Vector2 = Vector2((screenX - ls.x).toFloat(), (ls.y - screenY).toFloat()).nor()
        moveRotation = v.angle()
        //println("LS ${leftSticker?.x}:${leftSticker?.y} Screen ${screenX}:${screenY} MoveRotation ${moveRotation}")
        if(len > 100) {
            isMove = true
        }
        else {
            isMove = false
        }*/
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        //leftSticker = StickerPosition(screenX, screenY)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        //if(leftSticker != null) {
         //   isMove = false
         //   leftSticker = null
        //}
        return true
    }
*/
    val leftStickWidget = Touchpad(0f, Scene2DSkin.defaultSkin)
    val rightStickWidget = Touchpad(0f, Scene2DSkin.defaultSkin)
    val button = Button(Scene2DSkin.defaultSkin)
    override fun CreateControllerWidget(): Actor? {
        return table {
            setFillParent(true)
            add(leftStickWidget).expandX().left()
            add(button).right()
            add(rightStickWidget).expandX().right()
        }.align(Align.bottom)
    }

}