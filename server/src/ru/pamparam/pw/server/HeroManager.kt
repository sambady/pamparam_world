package ru.pamparam.pw.server

import ru.pamparam.pw.packets.*
import ru.pamparam.pw.server.world.ecscomponents.PositionComponent
import ru.pamparam.pw.server.world.WorldManager

object HeroManager
{
    private var newAccountId = 0
    private var newHeroId = 0
    private val accountByConnectionId = mutableMapOf<Int, Account>()
    private val accountById = mutableMapOf<Int, Account>()
    private val heroById = mutableMapOf<Int, Hero>()

    fun getHeroByConnectionId(connectionId: Int) : Hero? {
        return accountByConnectionId.get(connectionId)?.activeHero
    }

    fun forEachHero(visitor : (hero : Hero) -> Unit) {
        accountByConnectionId.forEach { connectionId, account ->
            val hero = account.activeHero
            if(hero != null) visitor(hero)
        }
    }

    fun handlePacket(client : Client, packet : ClpLogin) {
        when(packet) {
            is ClpCreateNewAccount -> OnCreateNewAccount(client, packet)
            is ClpCreateHeroAndSelect -> OnCreateNewHeroAndSelect(client, packet)
            is ClpSelectHero -> OnSelectHero(client, packet)
            is ClpLoginAndGetHeroList -> OnLoginAndGetHeroList(client, packet)
            is ClpCreateNewAccountHeroAndSelect -> OnClpCreateNewAccountHeroAndSelect(client, packet)
        }
    }

    fun connectionClosed(connectionId : Int) {
        val account = accountByConnectionId.get(connectionId)
        if(account == null)
            return

        val hero = account.activeHero
        if(hero != null) {
            WorldManager.worldNode.removeHero(hero.heroId)
            account.activeHero = null
        }
        accountByConnectionId.remove(connectionId)
    }

    fun sendBroadcast(svpPacket : ServerPacket) {
        accountByConnectionId.forEach {
            it.value.client?.SendPacket(svpPacket)
        }
    }

    private fun OnCreateNewAccount(client: Client, packet: ClpCreateNewAccount) {
        newAccountId += 1
        val account = Account(newAccountId)
        account.client = client
        accountByConnectionId[client.connectionId] = account
        accountById[account.accountId] = account

        println("Account created ${account.accountId}")

        client.SendPacket(SvpCreateNewAccount(account.accountId))
    }

    private fun OnCreateNewHeroAndSelect(client: Client, packet: ClpCreateHeroAndSelect) {
        val account = accountByConnectionId.get(client.connectionId)
        if(account == null) {
            println("OnCreateNewHeroAndSelect Account by connection ${client.connectionId} not found")
            return
        }
        newHeroId += 1

        val hero = Hero(newHeroId, account)
        heroById[newHeroId] = hero
        account.availableHeroIds.add(newHeroId)
        account.activeHero = hero

        val heroEntity = WorldManager.worldNode.addOrGetHero(hero)
        val position = heroEntity.getComponent(PositionComponent::class.java)
        if(position == null) {
            return
        }
        client.SendPacket(SvpLocalHeroInit(
                hero.heroId,
                position.x,
                position.y,
                position.rotation
        ))
        WorldManager.worldNode.SendToHero(hero)
    }

    private fun OnSelectHero(client: Client, packet : ClpSelectHero) {
        val account = accountByConnectionId.get(client.connectionId)
        if(account == null) {
            println("OnSelectHero Account by connection ${client.connectionId} not found")
            return
        }

        if(account.activeHero != null && account.activeHero?.heroId == packet.heroId) {
            println("OnSelectHero Account ${account.accountId} heroId ${account.activeHero?.heroId} already selected")
            return
        }

        if(account.availableHeroIds.find{it == packet.heroId} == null) {
            println("OnSelectHero account ${account.accountId} heroId ${packet.heroId} not created yet")
            return
        }

        val hero = heroById.get(packet.heroId)
        if(hero == null) {
            println("OnSelectHero ${account.accountId} heroId ${packet.heroId} hero not found")
            return
        }

        account.activeHero = hero

        val heroEntity = WorldManager.worldNode.addOrGetHero(hero)
        val position = heroEntity.getComponent(PositionComponent::class.java)
        if(position == null) {
            return
        }
        client.SendPacket(SvpLocalHeroInit(
                hero.heroId,
                position.x,
                position.y,
                position.rotation
        ))
        WorldManager.worldNode.SendToHero(hero)
    }

    private fun OnLoginAndGetHeroList(client: Client, packet: ClpLoginAndGetHeroList) {
        val account = accountById.get(packet.accountId)
        if(account == null) {
            println("OnGetHeroList Account ${packet.accountId} not found")
            return
        }
        //TODO close open connection...
        accountByConnectionId[client.connectionId] = account
        account.client = client
        account.activeHero = null
        // TODO Remove hero from world

        client.SendPacket(SvpHeroList(account.availableHeroIds))
    }

    private fun OnClpCreateNewAccountHeroAndSelect(client: Client, packet: ClpCreateNewAccountHeroAndSelect) {
        newAccountId += 1
        val account = Account(newAccountId)
        account.client = client
        accountByConnectionId[client.connectionId] = account
        accountById[account.accountId] = account

        println("Account created ${account.accountId}")

        newHeroId += 1
        val hero = Hero(newHeroId, account)
        heroById[newHeroId] = hero
        account.availableHeroIds.add(newHeroId)
        account.activeHero = hero

        val heroEntity = WorldManager.worldNode.addOrGetHero(hero)
        val position = heroEntity.getComponent(PositionComponent::class.java)
        if(position == null) {
            println("OnClpCreateNewAccountHeroAndSelect empty position ${account.accountId}")
            return
        }
        client.SendPacket(SvpLocalHeroInit(
                hero.heroId,
                position.x,
                position.y,
                position.rotation
        ))
        client.SendPacket(SvpCreateNewAccountAndHeroComplete(packet.requestId))
        WorldManager.worldNode.SendToHero(hero)
        println("Account ${account.accountId} hero ${newHeroId} CREATED")
    }

}