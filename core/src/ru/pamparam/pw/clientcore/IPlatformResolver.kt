package ru.pamparam.pw.clientcore

import ru.pamparam.pw.clientcore.screens.gameplay.GameplayScreen

interface IPlatformResolver {
    fun CreateHeroController(gamePlay: GameplayScreen) : IHeroController
}