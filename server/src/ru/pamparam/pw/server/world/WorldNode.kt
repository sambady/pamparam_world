package ru.pamparam.pw.server.world

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import ru.pamparam.pw.packets.*
import ru.pamparam.pw.server.Client
import ru.pamparam.pw.server.Hero
import ru.pamparam.pw.server.HeroManager
import ru.pamparam.pw.server.QuantumParams
import ru.pamparam.pw.server.world.ecscomponents.PositionComponent

class WorldNode {
    private val engine : Engine
    private val heroEntityById = mutableMapOf<Int, Entity>()

    init {
        engine = Engine()
    }

    fun addOrGetHero(hero : Hero) : Entity {
        var heroEntity = heroEntityById.get(hero.heroId)
        if (heroEntity == null) {
            heroEntity = Entity()
            val position = PositionComponent()
            heroEntity.add(position)
            engine.addEntity(heroEntity)
            heroEntityById[hero.heroId] = heroEntity
            hero.worldEntity = heroEntity

            val packet = SvpAddHero(heroId = hero.heroId,
                    x = position.x,
                    y = position.y,
                    rotation = position.rotation)
            HeroManager.forEachHero {
                if(it.heroId != hero.heroId) {
                    it.SendPacket(packet)
                }
            }
        }
        return heroEntity
    }

    fun SendToHero(hero : Hero) {
        heroEntityById.forEach { heroId, entity ->
            if(hero.heroId != heroId) {
                val position = entity.getComponent(PositionComponent::class.java)
                if (position != null) {
                    val addHero = SvpAddHero(heroId = heroId,
                            x = position.x,
                            y = position.y,
                            rotation = position.rotation,
                            isMove = position.isMove)
                    hero.SendPacket(addHero)
                }
            }
        }
    }

    fun removeHero(heroId : Int) {
        val heroEntityId = heroEntityById.get(heroId)
        if(heroEntityId == null)
            return

        engine.removeEntity(heroEntityId)
        heroEntityById.remove(heroId)
    }

    val heroStateToSend = mutableMapOf<Int, SvpHeroState>()

    var metrics = 0
    fun getMetricsAndFlush() : Int {
        val m = metrics
        metrics = 0
        return m
    }

    fun update(quantumParams: QuantumParams) {
        for((heroId, state) in heroStateToSend) {
            HeroManager.forEachHero {
                if(it.heroId != heroId) {
                    it.SendPacket(state)
                }
            }
        }
        metrics += heroStateToSend.size
        heroStateToSend.clear()
    }

    fun handleActorSyncPacket(client : Client, packet : ClpActorSync) {

        val hero = HeroManager.getHeroByConnectionId(client.connectionId)
        if(hero == null)
            return

        when(packet) {
            is ClpHeroState -> onClpHeroState(hero, packet)
        }
    }

    private fun onClpHeroState(hero : Hero, packet : ClpHeroState) {
        heroStateToSend[hero.heroId] = ClpToSvpState(hero, packet)
    }

    private fun ClpToSvpState(hero : Hero, clpHeroState : ClpHeroState) : SvpHeroState
    {
        return when(clpHeroState) {
            is ClpHeroStateWeaponPrimaryAttack -> {
                SvpHeroStateWeaponPrimaryAttack().let {
                    it.heroId = hero.heroId
                    it.x = clpHeroState.x
                    it.y = clpHeroState.y
                    it.moveRotation = clpHeroState.moveRotation
                    it.lookRotation = clpHeroState.lookRotation
                    it.isMove = clpHeroState.isMove
                    it.actionId = clpHeroState.actionId
                    it
                }
            }
            is ClpHeroStateWeaponSecondaryAttack -> {
                SvpHeroStateWeaponSecondaryAttack().let {
                    it.heroId = hero.heroId
                    it.x = clpHeroState.x
                    it.y = clpHeroState.y
                    it.moveRotation = clpHeroState.moveRotation
                    it.lookRotation = clpHeroState.lookRotation
                    it.isMove = clpHeroState.isMove
                    it.actionId = clpHeroState.actionId
                    it
                }
            }
            is ClpHeroStateWeaponChange -> {
                SvpHeroStateWeaponChange().let {
                    it.heroId = hero.heroId
                    it.x = clpHeroState.x
                    it.y = clpHeroState.y
                    it.moveRotation = clpHeroState.moveRotation
                    it.lookRotation = clpHeroState.lookRotation
                    it.isMove = clpHeroState.isMove
                    it.weaponAction = clpHeroState.weaponAction
                    it.actionId = clpHeroState.actionId
                    it
                }
            }
            is ClpHeroStateWeaponReload -> {
                SvpHeroStateWeaponReload().let {
                    it.heroId = hero.heroId
                    it.x = clpHeroState.x
                    it.y = clpHeroState.y
                    it.moveRotation = clpHeroState.moveRotation
                    it.lookRotation = clpHeroState.lookRotation
                    it.isMove = clpHeroState.isMove
                    it.actionId = clpHeroState.actionId
                    it
                }
            }
            else -> {
                SvpHeroState().let {
                    it.heroId = hero.heroId
                    it.x = clpHeroState.x
                    it.y = clpHeroState.y
                    it.moveRotation = clpHeroState.moveRotation
                    it.lookRotation = clpHeroState.lookRotation
                    it.isMove = clpHeroState.isMove
                    it
                }
            }
        }

    }
}