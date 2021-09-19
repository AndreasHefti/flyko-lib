package com.inari.firefly.game.player

import com.inari.firefly.FFContext
import com.inari.firefly.game.player.PlayerSystem.UNDEFINED_PLAYER_ASPECT
import com.inari.util.aspect.Aspect
import com.inari.util.event.Event

typealias PlayerEventListener = (type: PlayerEventType) -> Unit
enum class PlayerEventType {
    PLAYER_LOADED,
    PLAYER_ACTIVATED,
    PLAYER_DEACTIVATED,
    PLAYER_DELETED,
    PLAYER_CHANGED,
}
class PlayerEvent(override val eventType: Event.EventType) : Event<PlayerEventListener>() {

    var type: PlayerEventType = PlayerEventType.PLAYER_LOADED
        private set
    var changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT
        private set

    override fun notify(listener: PlayerEventListener) = listener(type)

    companion object : EventType("PlayerEvent") {
        private val playerEvent = PlayerEvent(this)
        fun send(type: PlayerEventType, changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT) {
            playerEvent.type = type
            playerEvent.changeAspect = changeAspect
            FFContext.notify(playerEvent)
        }
    }
}