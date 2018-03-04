package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import ru.pamparam.pw.common.WeaponActionType
import java.util.*


enum class WeaponShootResult
{
    UNKNOWN,
    MISS,
    SUCCESS
}

fun NetworkHeroControllerComponent.addState(state : NetworkHeroControllerComponent.State) {
    stateQueue.add(state)
}

class NetworkHeroControllerComponent() : Component
{
    var lastUpdatedServerQuant = 0
    data class State(
        val serverQuantNumber : Int = 0,
        val x : Float = 0f,
        val y : Float = 0f,
        val isMove : Boolean = false,
        val moveRotation : Float = 0f,
        val lookRotation : Float = 0f,
        val weaponAction : WeaponActionType = WeaponActionType.none,
        val weaponActionId : Int = 0,
        val targetId : Int = 0
    )

    val stateQueue : ArrayDeque<State> = ArrayDeque<State>()

    /*
    private val stateQueue : Queue<State> = LinkedList<State>()

    fun addState(state : State) {
        if(!stateQueue.isEmpty() && stateQueue.last().serverQuantNumber > state.serverQuantNumber) {
            println("addState ${stateQueue.last().serverQuantNumber} > ${state.serverQuantNumber}")
        }
        stateQueue.add(state)
    }

    fun getCurrentState(currentServerQuantNumber : Int) : State? {
        if(stateQueue.isEmpty())
            return null
        if(stateQueue.first().serverQuantNumber + 4 < currentServerQuantNumber) {
            val state = stateQueue.remove()
            return state
        }
        else {
            if(stateQueue.size > 10) {
                println("getCurrentState ${stateQueue.size} ${stateQueue.first().serverQuantNumber} ${currentServerQuantNumber}")
            }
            return null
        }
    }

    private var weaponCommandQueue : Queue<ExtendedWeaponCommand> = LinkedList<ExtendedWeaponCommand>()
    fun addCommand(weaponCommand : ExtendedWeaponCommand) {
        if(!weaponCommandQueue.isEmpty() && stateQueue.last().serverQuantNumber > weaponCommand.serverQuantNumber) {
            println("addCommand ${stateQueue.last().serverQuantNumber} > ${weaponCommand.serverQuantNumber}")
        }
        weaponCommandQueue.add(weaponCommand)
    }

    fun getAndFlushWeaponCommand(currentServerQuantNumber : Int): ExtendedWeaponCommand? {
        if(weaponCommandQueue.isEmpty())
            return null
        if(weaponCommandQueue.first().serverQuantNumber + 4 < currentServerQuantNumber) {
            return weaponCommandQueue.remove()
        }
        else {
            if(stateQueue.size > 10) {
                println("GetAndFlushWeaponCommand ${stateQueue.size} ${stateQueue.first().serverQuantNumber} ${currentServerQuantNumber}")
            }
            return null
        }
    }
    */
}