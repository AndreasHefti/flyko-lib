package com.inari.firefly.misc

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.util.event.Event

typealias TestEventListener = (CompId) -> Unit
class StaticTestEvent(override val eventType: EventType) : Event<TestEventListener>() {

    var id: CompId? = null

    override fun notify(listener: TestEventListener) =
        listener(id!!)

    companion object : EventType("StaticTestEvent") {

        fun send(init: StaticTestEvent.() -> Unit) {
            val event = StaticTestEvent(this)
            event.also(init)
            FFContext.notify(event)
        }
    }
}