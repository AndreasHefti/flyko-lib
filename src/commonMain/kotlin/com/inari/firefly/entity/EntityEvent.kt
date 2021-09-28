package com.inari.firefly.entity

import com.inari.firefly.FFContext

import com.inari.util.aspect.Aspects
import com.inari.util.event.AspectedEvent
import com.inari.util.event.AspectedEventListener

interface EntityEventListener : AspectedEventListener {
    fun entityOnCreation(entity: Entity) {}
    fun entityActivated(entity: Entity) {}
    fun entityDeactivated(entity: Entity) {}
    fun entityOnDispose(entity: Entity) {}
}
enum class EntityEventType {
    ON_CREATE,
    ACTIVATED,
    DEACTIVATED,
    ON_DISPOSE
}
class EntityEvent(override val eventType: EventType) : AspectedEvent<EntityEventListener>() {

    lateinit var entity: Entity
        internal set
    lateinit var type: EntityEventType
        internal set

    override val aspects: Aspects get() =
        entity.aspects

    override fun notify(listener: EntityEventListener) =
        when(type) {
            EntityEventType.ON_CREATE -> listener.entityOnCreation(entity)
            EntityEventType.ACTIVATED -> listener.entityActivated(entity)
            EntityEventType.DEACTIVATED -> listener.entityDeactivated(entity)
            EntityEventType.ON_DISPOSE -> listener.entityOnDispose(entity)
        }

    companion object : EventType("EntityEvent") {
        internal val entityEvent = EntityEvent(this)
        fun send(entity: Entity, type: EntityEventType) {
            entityEvent.entity = entity
            entityEvent.type = type
            FFContext.notify(entityEvent)
        }
    }
}