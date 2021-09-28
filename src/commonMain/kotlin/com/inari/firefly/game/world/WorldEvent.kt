package com.inari.firefly.game.world


import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.event.Event

typealias WorldEventListener = (type: WorldEventType, id: CompId) -> Unit
enum class WorldEventType { WORLD_LOADED, WORLD_ACTIVATED, WORLD_DEACTIVATED, WORLD_DISPOSED }
class WorldEvent(override val eventType: Event.EventType) : Event<WorldEventListener>() {

    var compId: CompId = NO_COMP_ID
        private set
    var type: WorldEventType = WorldEventType.WORLD_LOADED
        private set

    override fun notify(listener: WorldEventListener) = listener(type, compId)

    companion object : EventType("WorldEvent") {
        private val worldEvent = WorldEvent(this)
        fun send(type: WorldEventType, id: CompId) {
            worldEvent.compId = id
            worldEvent.type = type
            FFContext.notify(worldEvent)
        }
    }
}