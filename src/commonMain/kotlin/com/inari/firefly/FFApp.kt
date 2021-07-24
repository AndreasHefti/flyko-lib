package com.inari.firefly


import com.inari.firefly.FFApp.PostRenderEvent.Companion.postRenderEvent
import com.inari.firefly.FFApp.RenderEvent.Companion.renderEvent
import com.inari.firefly.FFApp.UpdateEvent.Companion.updateEvent
import com.inari.firefly.core.api.*
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.util.Call
import com.inari.util.Consumer
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.event.Event
import com.inari.util.event.IEventDispatcher
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField
import kotlin.math.floor

abstract class FFApp protected constructor(
    eventDispatcher: () -> IEventDispatcher,
    graphics: () -> GraphicsAPI,
    audio: () -> AudioAPI,
    input: () -> InputAPI,
    timer: () -> TimerAPI
) {

    private var initialized: Boolean = false

    init {
        if (initialized) {
            throw IllegalStateException("FFApp is a conceptual singleton and is already initialized")
        }

        Companion.eventDispatcher = eventDispatcher()
        Companion.graphics = graphics()
        Companion.audio = audio()
        Companion.input = input()
        Companion.timer = timer()
    }

    fun update() {
        timer.tick()
        FFContext.notify(updateEvent)
        timer.updateSchedulers()
    }

    fun render() {
            val size = ViewSystem.activeViewPorts.size
            if (size > 0) {
                var i = 0
                while (i < size) {
                    ViewSystem.activeViewPorts[i++]?.apply {
                        render(this)
                    }
                }

                graphics.flush(ViewSystem.activeViewPorts)
            } else {
                render(ViewSystem.baseView.data)
                graphics.flush(NO_VIRTUAL_VIEW_PORTS)
            }

            FFContext.notify(postRenderEvent)
    }

    private fun render(view: ViewData) {
        renderEvent.viewIndex = view.index
        renderEvent.layerIndex = 0
        renderEvent.clip(
            floor(view.worldPosition.x.toDouble()).toInt(),
            floor(view.worldPosition.y.toDouble()).toInt(),
            view.bounds.width,
            view.bounds.height
        )

        graphics.startRendering(view, true)

        ViewSystem.layersOfView[view.index]?.apply {
            if (isEmpty) {
                FFContext.notify(renderEvent)
            } else {
                val layerIterator = iterator()
                while (layerIterator.hasNext()) {
                    val layerId = layerIterator.next()
                    if (!ViewSystem.layers.isActive(layerId))
                        continue
                    renderEvent.layerIndex = layerId
                    FFContext.notify(renderEvent)
                }
            }
        }

        graphics.endRendering(view)
    }



    companion object {

        lateinit var eventDispatcher: IEventDispatcher
            private set
        lateinit var graphics: GraphicsAPI
            private set
        lateinit var audio: AudioAPI
            private set
        lateinit var input: InputAPI
            private set
        lateinit var timer: TimerAPI
            private set

        @JvmField internal val NO_VIRTUAL_VIEW_PORTS: DynArrayRO<ViewData> = DynArray.of()
    }

    class UpdateEvent(override val eventType: EventType) : Event<Call>() {
        override fun notify(listener: Call) = listener()
        companion object : EventType("UpdateEvent") {
            internal val updateEvent = UpdateEvent(this)
        }
    }

    class RenderEvent(override val eventType: EventType) : Event<Consumer<RenderEvent>>() {

        var viewIndex: Int = -1
            internal set
        var layerIndex: Int = -1
            internal set
        var clip: Rectangle = Rectangle(0, 0, 0, 0)
            internal set

        override fun notify(listener: Consumer<RenderEvent>) = listener.invoke(this)

        companion object : EventType("RenderEvent") {
            internal val renderEvent = RenderEvent(this)
        }
    }

    class PostRenderEvent(override val eventType: EventType) : Event<Call>() {
        override fun notify(listener: Call) = listener()
        companion object : EventType("PostRenderEvent") {
            internal val postRenderEvent = PostRenderEvent(this)
        }
    }
}