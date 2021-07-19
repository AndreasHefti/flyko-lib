package com.inari.firefly.physics.contact

import com.inari.util.IntConsumer
import com.inari.util.event.Event


class ContactEvent(override val eventType: EventType) : Event<IntConsumer>() {

    var entity: Int = -1
        internal set

    override fun notify(listener: IntConsumer) { listener(entity) }

    companion object : EventType("ContactEvent") {
        internal val contactEvent = ContactEvent(this)
    }
}