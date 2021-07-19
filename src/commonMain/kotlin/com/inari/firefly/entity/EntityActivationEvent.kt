package com.inari.firefly.entity

import com.inari.firefly.FFContext

import com.inari.util.aspect.Aspects
import com.inari.util.event.AspectedEvent
import com.inari.util.event.AspectedEventListener
import com.inari.util.event.Event

class EntityActivationEvent(override val eventType: EventType) : AspectedEvent<EntityActivationEvent.Listener>() {

    enum class Type {
        ACTIVATED,
        DEACTIVATED
    }

    lateinit var entity: Entity
        internal set
    lateinit var type: Type
        internal set

    override val aspects: Aspects get() =
        entity.aspects

    override fun notify(listener: Listener) =
        when(type) {
            Type.ACTIVATED -> listener.entityActivated(entity)
            Type.DEACTIVATED -> listener.entityDeactivated(entity)
        }



    interface Listener : AspectedEventListener {
        fun entityActivated(entity: Entity)
        fun entityDeactivated(entity: Entity)
    }

    companion object : EventType("EntityActivationEvent") {
        internal val entityActivationEvent = EntityActivationEvent(this)
        fun send(entity: Entity, type: Type) {
            entityActivationEvent.entity = entity
            entityActivationEvent.type = type
            FFContext.notify(entityActivationEvent)
        }
    }
}