package ru.pamparam.pw.clientcore.screens.gameplay.heroecs.components

import com.badlogic.ashley.core.Component
import ru.pamparam.pw.clientcore.IHeroController
import ru.pamparam.pw.packets.ClpHeroState


class LocalHeroControllerComponent(val controller : IHeroController) : Component
{
    var lastSendState : ClpHeroState? = null
}