package com.inari.firefly.graphics.view

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.util.Consumer
import com.inari.util.event.Event
import kotlin.jvm.JvmField

class ViewChangeEvent(override val eventType: EventType) : Event<Consumer<ViewChangeEvent>>() {

    enum class Type {
        POSITION,
        ORIENTATION,
        SIZE
    }

    var id: CompId = NO_COMP_ID
        private set
    var type: Type = Type.POSITION
        private set
    var pixelPerfect = false
        private set

    override fun notify(listener: Consumer<ViewChangeEvent>) = listener(this)

    companion object : EventType("ViewChangeEvent") {
        internal val viewChangeEvent = ViewChangeEvent(this)
        fun of(id: CompId, type: Type, pixelPerfect: Boolean): ViewChangeEvent {
            val viewChangeEvent = ViewChangeEvent(this)
            viewChangeEvent.id = id
            viewChangeEvent.type = type
            viewChangeEvent.pixelPerfect = pixelPerfect
            return viewChangeEvent
        }
        fun send(id: CompId, type: Type, pixelPerfect: Boolean) {
            viewChangeEvent.id = id
            viewChangeEvent.type = type
            viewChangeEvent.pixelPerfect = pixelPerfect
            FFContext.notify(viewChangeEvent)
        }
    }
}