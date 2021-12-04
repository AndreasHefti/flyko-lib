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
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.floor

abstract class FFApp protected constructor(
    eventDispatcher: () -> IEventDispatcher,
    graphics: () -> GraphicsAPI,
    audio: () -> AudioAPI,
    input: () -> InputAPI,
    timer: () -> TimerAPI,
    resourceService: () -> ResourceServiceAPI
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
        Companion.resourceService = resourceService()
    }

    fun update() {
        timer.tick()
        FFContext.notify(updateEvent)
        timer.updateSchedulers()
    }

    fun render() {
        if (!ViewSystem.frameBuffers.isEmpty)
            renderFrameBuffer()

        renderEvent.frameBufferId = -1
        val size = ViewSystem.privateActiveViewPorts.size
        if (size > 0) {
            ViewSystem.privateActiveViewPorts.forEach { renderViewport(it) }
            graphics.flush(ViewSystem.privateActiveViewPorts)
        } else {
            renderViewport(ViewSystem.baseView.data)
            graphics.flush(NO_VIRTUAL_VIEW_PORTS)
        }

        FFContext.notify(postRenderEvent)
    }

    private fun renderViewport(view: ViewData) {

        renderEvent.viewIndex = view.index
        renderEvent.layerIndex = 0
        renderEvent.clip(
            floor(view.worldPosition.x.toDouble()).toInt(),
            floor(view.worldPosition.y.toDouble()).toInt(),
            view.bounds.width,
            view.bounds.height
        )

        // view rendering
        graphics.startViewportRendering(view, true)
        ViewSystem.privateLayersOfView[view.index]?.apply {
            if (isEmpty)
                FFContext.notify(renderEvent)
            else {
                val layerIterator = iterator()
                while (layerIterator.hasNext()) {
                    val layerId = layerIterator.next().index
                    if (!ViewSystem.layers.isActive(layerId))
                        continue

                    FFContext.graphics.setActiveShader(ViewSystem.layers[layerId].shaderRef)
                    renderEvent.layerIndex = layerId
                    FFContext.notify(renderEvent)
                }
            }
        }
        graphics.endViewportRendering(view)
    }

    private fun renderFrameBuffer() {
        renderEvent.viewIndex = -1
        renderEvent.layerIndex = -1
        ViewSystem.frameBuffers.forEachActive { frameBuffer ->
            graphics.startFrameBufferRendering(frameBuffer.bufferId, frameBuffer.bounds.x, frameBuffer.bounds.y, frameBuffer.clear)
            renderEvent.clip(frameBuffer.bounds)
            renderEvent.frameBufferId = frameBuffer.bufferId
            FFContext.notify(renderEvent)
            graphics.endFrameBufferRendering(frameBuffer.bufferId)
        }
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
        lateinit var resourceService: ResourceServiceAPI
            private set

        @JvmField internal val NO_VIRTUAL_VIEW_PORTS: DynArrayRO<ViewData> = DynArray.of()
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class UpdateEvent(override val eventType: EventType) : Event<Call>() {
        override inline fun notify(listener: Call) = listener()
        companion object : EventType("UpdateEvent") {
            internal val updateEvent = UpdateEvent(this)
        }
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class RenderEvent(override val eventType: EventType) : Event<Consumer<RenderEvent>>() {

        var viewIndex: Int = -1
            internal set
        var layerIndex: Int = -1
            internal set
        var clip: Vector4i = Vector4i(0, 0, 0, 0)
            internal set
        var frameBufferId: Int = -1
            internal set

        override inline fun notify(listener: Consumer<RenderEvent>) = listener(this)

        companion object : EventType("RenderEvent") {
            internal val renderEvent = RenderEvent(this)
        }
    }

    @Suppress("OVERRIDE_BY_INLINE")
    class PostRenderEvent(override val eventType: EventType) : Event<Call>() {
        override inline fun notify(listener: Call) = listener()
        companion object : EventType("PostRenderEvent") {
            internal val postRenderEvent = PostRenderEvent(this)
        }
    }
}