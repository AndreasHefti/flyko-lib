package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.event.Event
import kotlin.reflect.KClass

typealias CompositeEventListener = (
    type: CompositeEventType,
    compId: CompId,
    compositeType: KClass<out SystemComponent>) -> Unit
enum class CompositeEventType {
    CREATED,
    LOADED,
    ACTIVATED,
    DEACTIVATED,
    DISPOSED,
    DELETED,
}
class CompositeEvent(override val eventType: EventType) : Event<CompositeEventListener>() {

    var type: CompositeEventType = CompositeEventType.CREATED
        private set
    var compId: CompId = NO_COMP_ID
        private set
    var compositeType: KClass<out SystemComponent> = Composite.typeClass

    override fun notify(listener: CompositeEventListener) = listener(type, compId, compositeType)

    companion object : EventType("PlayerEvent") {
        private val compositeEvent = CompositeEvent(this)
        fun send(type: CompositeEventType, compId: CompId, compositeType: KClass<out SystemComponent> = Composite.typeClass) {
            compositeEvent.type = type
            compositeEvent.compId = compId
            compositeEvent.compositeType = compositeType
            FFContext.notify(compositeEvent)
        }
    }
}