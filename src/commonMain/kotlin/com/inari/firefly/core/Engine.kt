package com.inari.firefly.core

import com.inari.firefly.core.api.*
import com.inari.util.collection.Attributes
import com.inari.util.event.*
import kotlin.jvm.JvmField

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
        notify(renderEvent)
        notify(postRenderEvent)
    }

    companion object {
        const val SYSTEM_FONT_ASSET = "SYSTEM_FONT_ASSET"
        const val SYSTEM_FONT = "SYSTEM_FONT"
        val GLOBAL_VALUES = Attributes()

        const val GLOBAL_SHOW_GIZMOS = "showGizmos"


        @JvmField val INFINITE_SCHEDULER: FFTimer.Scheduler = object : FFTimer.Scheduler {
            override val resolution: Float = 60f
            override fun needsUpdate(): Boolean = true
        }

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

        val UPDATE_EVENT_TYPE = Event.EventType("UpdateEvent")
        val RENDER_EVENT_TYPE = Event.EventType("RenderingEvent")
        val POST_RENDER_EVENT_TYPE = Event.EventType("PostRenderEvent")

        internal val updateEvent = UpdateEvent(UPDATE_EVENT_TYPE)
        internal val renderEvent = RenderingEvent(RENDER_EVENT_TYPE)
        internal val postRenderEvent = UpdateEvent(POST_RENDER_EVENT_TYPE)
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class UpdateEvent(override val eventType: EventType) : Event<() -> Unit>() {
        override inline fun notify(listener: () -> Unit) = listener()
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class RenderingEvent(override val eventType: EventType) : Event<() -> Unit>() {
        override inline fun notify(listener: () -> Unit) = listener()
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class PostRenderEvent private constructor(override val eventType: EventType) : Event<() -> Unit>() {
        override inline fun notify(listener: () -> Unit) = listener()
    }
}