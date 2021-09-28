package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspect
import com.inari.util.event.Event
import kotlin.reflect.KClass

typealias CompositeEventListener = (
    type: CompositeEventType,
    compId: CompId,
    compositeAspect: Aspect) -> Unit
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
    var compositeAspect: Aspect = Composite

    override fun notify(listener: CompositeEventListener) = listener(type, compId, compositeAspect)

    companion object : EventType("CompositeEvent") {
        private val compositeEvent = CompositeEvent(this)
        fun send(type: CompositeEventType, compId: CompId, compositeAspect: Aspect = Composite) {
            compositeEvent.type = type
            compositeEvent.compId = compId
            compositeEvent.compositeAspect = compositeAspect
            FFContext.notify(compositeEvent)
        }
    }
}