package com.inari.firefly.graphics.view

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.Consumer
import com.inari.util.event.Event

class ViewChangeEvent(override val eventType: EventType) : Event<Consumer<ViewChangeEvent>>() {

    enum class Type {
        POSITION,
        ORIENTATION,
        SIZE
    }

    internal var id: CompId = NO_COMP_ID
    internal var type: Type = Type.POSITION

    override fun notify(listener: Consumer<ViewChangeEvent>) = listener(this)

    companion object : EventType("ViewChangeEvent") {
        internal val viewChangeEvent = ViewChangeEvent(this)
        fun send(id: CompId, type: Type) {
            viewChangeEvent.id = id
            viewChangeEvent.type = type
            FFContext.notify(viewChangeEvent)
        }
    }
}