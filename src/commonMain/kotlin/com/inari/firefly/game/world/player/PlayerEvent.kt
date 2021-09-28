package com.inari.firefly.game.world.player

import com.inari.firefly.FFContext
import com.inari.firefly.game.world.RoomKey
import com.inari.firefly.game.world.WorldSystem
import com.inari.firefly.game.world.player.PlayerSystem.UNDEFINED_PLAYER_ASPECT
import com.inari.util.aspect.Aspect
import com.inari.util.event.Event

typealias PlayerEventListener = (event: PlayerEvent) -> Unit
enum class PlayerEventType {
    PLAYER_LOADED,
    PLAYER_ACTIVATED,
    PLAYER_ENTRY,
    PLAYER_CHANGED,
    PLAYER_EXIT,
    PLAYER_DEACTIVATED,
    PLAYER_DELETED
}
class PlayerEvent(override val eventType: EventType) : Event<PlayerEventListener>() {

    var type: PlayerEventType = PlayerEventType.PLAYER_LOADED
        private set
    var changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT
        private set
    var room: RoomKey = RoomKey()

    override fun notify(listener: PlayerEventListener) = listener(this)

    companion object : EventType("PlayerEvent") {
        private val playerEvent = PlayerEvent(this)
        fun send(type: PlayerEventType, changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT) {
            playerEvent.type = type
            playerEvent.changeAspect = changeAspect
            FFContext.notify(playerEvent)
        }
    }
}