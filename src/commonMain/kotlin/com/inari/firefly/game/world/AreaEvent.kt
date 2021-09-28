package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.event.Event

typealias AreaEventListener = (type: AreaEventType, id: CompId) -> Unit
enum class AreaEventType { AREA_LOADED, AREA_ACTIVATED, AREA_DEACTIVATED, AREA_DISPOSED }
class AreaEvent(override val eventType: Event.EventType) : Event<AreaEventListener>() {

    var compId: CompId = NO_COMP_ID
        private set
    var type: AreaEventType = AreaEventType.AREA_LOADED
        private set

    override fun notify(listener: AreaEventListener) = listener(type, compId)

    companion object : EventType("AreaEvent") {
        private val areaEvent = AreaEvent(this)
        fun send(type: AreaEventType, id: CompId) {
            areaEvent.compId = id
            areaEvent.type = type
            FFContext.notify(areaEvent)
        }
    }
}