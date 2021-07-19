package com.inari.firefly.graphics.view

import com.inari.firefly.FFContext
import com.inari.firefly.core.api.ViewData
import com.inari.firefly.core.component.CompId
import com.inari.util.Consumer
import com.inari.util.event.Event

class ViewEvent(override val eventType: EventType) : Event<Consumer<ViewEvent>>() {

    enum class Type {
        VIEW_CREATED,
        VIEW_ACTIVATED,
        VIEW_DISPOSED,
        VIEW_DELETED
    }

    lateinit var id: CompId
        internal set
    lateinit var data: ViewData
        internal set
    lateinit var type: Type
        internal set

    override fun notify(listener: Consumer<ViewEvent>) = listener(this)

    companion object : EventType("ViewEvent") {
        internal val viewEvent = ViewEvent(this)
        internal fun send(id: CompId, data: ViewData, type: Type) {
            viewEvent.id = id
            viewEvent.data = data
            viewEvent.type = type
            FFContext.notify(viewEvent)
        }
    }
}