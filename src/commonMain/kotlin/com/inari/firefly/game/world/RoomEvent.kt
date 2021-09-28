package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.event.Event

typealias RoomEventListener = (type: RoomEventType, id: CompId) -> Unit
enum class RoomEventType {
    ROOM_LOADED, ROOM_ACTIVATED,
    ROOM_DEACTIVATED, ROOM_DISPOSED,
    ROOM_PAUSED, ROOM_RESUMED
}
class RoomEvent(override val eventType: Event.EventType) : Event<RoomEventListener>() {

    var compId: CompId = NO_COMP_ID
        private set
    var type: RoomEventType = RoomEventType.ROOM_LOADED
        private set

    override fun notify(listener: RoomEventListener) = listener(type, compId)

    companion object : EventType("RoomEvent") {
        private val roomEvent = RoomEvent(this)
        fun send(type: RoomEventType, id: CompId) {
            roomEvent.compId = id
            roomEvent.type = type
            FFContext.notify(roomEvent)
        }
    }
}