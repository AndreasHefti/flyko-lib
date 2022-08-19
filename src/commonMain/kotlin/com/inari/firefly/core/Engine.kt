package com.inari.firefly.core


import com.inari.firefly.core.Engine.UpdateEvent.Companion.updateEvent
import com.inari.firefly.core.PostRenderEvent.Companion.postRenderEvent
import com.inari.firefly.core.RenderingEvent.Companion.renderingEvent
import com.inari.firefly.core.api.*
import com.inari.util.event.*

abstract class Engine protected constructor(
    graphics: () -> GraphicsAPI,
    audio: () -> AudioAPI,
    input: () -> InputAPI,
    timer: () -> TimerAPI,
    resourceService: () -> ResourceServiceAPI
) {

    private var initialized: Boolean = false

    init {
        if (initialized) {
            throw IllegalStateException("Engine is a conceptual singleton and is already initialized")
        }

        Companion.graphics = graphics()
        Companion.audio = audio()
        Companion.input = input()
        Companion.timer = timer()
        Companion.resourceService = resourceService()
    }

    fun update() {
        // update objects
        timer.tick()
        notify(updateEvent)
        timer.updateSchedulers()
        // render objects
        notify(renderingEvent)
        notify(postRenderEvent)
    }

    companion object {
        const val SYSTEM_FONT_ASSET = "SYSTEM_FONT_ASSET"
        const val SYSTEM_FONT = "SYSTEM_FONT"

        private val eventDispatcher = EventDispatcher()
        lateinit var graphics: GraphicsAPI
            private set
        lateinit var audio: AudioAPI
            private set
        lateinit var input: InputAPI
            private set
        lateinit var timer: TimerAPI
            private set
        lateinit var resourceService: ResourceServiceAPI
            private set

        fun <L> registerListener(event: Event<L>, listener: L) = eventDispatcher.register(event.eventType, listener)
        fun <L> registerListener(eventType: Event.EventType, listener: L) = eventDispatcher.register(eventType, listener)
        fun <L> disposeListener(event: Event<L>, listener: L) = this.disposeListener(event.eventType, listener)
        fun <L> disposeListener(eventType: Event.EventType, listener: L) {
            if (!eventDispatcher.unregister(eventType, listener))
                throw IllegalArgumentException("Listener not disposed, eventType: $eventType listener: $listener" )
        }
        fun <L> notify(event: Event<L>) = eventDispatcher.notify(event)
        fun <L : AspectedEventListener> notify(event: AspectedEvent<L>) = eventDispatcher.notify(event)
        
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class UpdateEvent(override val eventType: EventType) : Event<() -> Unit>() {
        override inline fun notify(listener: () -> Unit) = listener()
        companion object : EventType("UpdateEvent") {
            internal val updateEvent = UpdateEvent(this)
        }
    }
}