package com.inari.firefly.physics.movement

import com.inari.util.Consumer
import com.inari.util.collection.BitSet
import com.inari.util.event.Event

class MoveEvent(override val eventType: EventType) : Event<Consumer<MoveEvent>>() {

    val entities: BitSet = BitSet(100)

    override fun notify(listener: Consumer<MoveEvent>) = listener(this)

    companion object : EventType("MoveEvent") {
        internal val moveEvent = MoveEvent(this)
    }
}