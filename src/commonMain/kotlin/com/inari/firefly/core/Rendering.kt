package com.inari.firefly.core

import com.inari.firefly.core.api.TransformData
import com.inari.util.ZERO_FLOAT
import com.inari.util.event.Event
import com.inari.util.geom.Vector2f
import kotlin.math.floor

abstract class Renderer {

    open fun activate() = Engine.registerListener(RenderingEvent, ::render)
    open fun deactivate() = Engine.disposeListener(RenderingEvent, ::render)
    abstract fun render()

}

@Suppress("OVERRIDE_BY_INLINE")
class RenderingEvent private constructor(override val eventType: EventType) : Event<() -> Unit>() {
    override inline fun notify(listener: () -> Unit) = listener()
    companion object : EventType("RenderingEvent") {
        internal val renderingEvent = RenderingEvent(this)
    }
}

@Suppress("OVERRIDE_BY_INLINE")
class PostRenderEvent private constructor(override val eventType: EventType) : Event<() -> Unit>() {
    override inline fun notify(listener: () -> Unit) = listener()
    companion object : EventType("PostRenderEvent") {
        internal val postRenderEvent = PostRenderEvent(this)
    }
}

class Transform constructor(
    override val position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT),
    override val pivot: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT),
    override val scale: Vector2f = Vector2f(1.0f, 1.0f),
    override var rotation: Float = ZERO_FLOAT
) : TransformData {

    operator fun plus(td: TransformData): Transform {
        position + td.position
        pivot + td.pivot
        scale * td.scale
        rotation += td.rotation
        return this
    }

    fun reset() {
        position.x = ZERO_FLOAT
        position.y = ZERO_FLOAT
        pivot.x = ZERO_FLOAT
        pivot.y = ZERO_FLOAT
        scale.v0 = 1.0f
        scale.v1 = 1.0f
        rotation = ZERO_FLOAT
    }
}

interface TransformDataCollector {
    val data : TransformData
    operator fun invoke(transform: TransformData)
    operator fun invoke(position: Vector2f)
    operator fun set(offset: Vector2f, transform: TransformData)
    operator fun plus(transform: TransformData)
    operator fun plus(offset: Vector2f)
    operator fun minus(offset: Vector2f)
    fun move(dx: Float, dy:Float)
}

class ExactTransformDataCollector internal constructor() : TransformDataCollector {
    override val data = Transform()

    override operator fun invoke(transform: TransformData) {
        data.position(transform.position)
        data.pivot(transform.pivot)
        data.scale(transform.scale)
        data.rotation = transform.rotation
    }

    override operator fun invoke(position: Vector2f) {
        data.position + position
    }

    override operator fun set(offset: Vector2f, transform: TransformData) {
        this(transform)
        data.position + offset
    }

    override operator fun plus(transform: TransformData) {
        data + transform
    }

    override operator fun plus(offset: Vector2f) {
        data.position + offset
    }

    override fun minus(offset: Vector2f) {
        data.position - offset
    }

    override fun move(dx: Float, dy: Float) {
        data.position.x += dx
        data.position.y += dy
    }
}

class DiscreteTransformDataCollector internal constructor() : TransformDataCollector {

    override val data = Transform()

    override operator fun invoke(transform: TransformData) {
        data.position(
            floor(transform.position.x.toDouble()).toFloat(),
            floor(transform.position.y.toDouble()).toFloat()
        )
        data.pivot(
            floor(transform.pivot.x.toDouble()).toFloat(),
            floor(transform.pivot.y.toDouble()).toFloat()
        )
        data.scale(transform.scale)
        data.rotation = transform.rotation
    }

    override operator fun invoke(position: Vector2f) {
        data.position(
            floor(position.x.toDouble()).toFloat(),
            floor(position.y.toDouble()).toFloat()
        )
    }

    override operator fun set(offset: Vector2f, transform: TransformData) {
        this(transform)
        data.position(
            floor((transform.position.x + offset.x).toDouble()).toFloat(),
            floor((transform.position.y + offset.y).toDouble()).toFloat()
        )
    }

    override operator fun plus(transform: TransformData) {
        data.position.x += floor(transform.position.x.toDouble()).toFloat()
        data.position.y += floor(transform.position.y.toDouble()).toFloat()
        data.pivot.x += floor(transform.pivot.x.toDouble()).toFloat()
        data.pivot.y += floor(transform.pivot.y.toDouble()).toFloat()
        data.scale.v0 *= transform.scale.v0
        data.scale.v1 *= transform.scale.v1
        data.rotation += transform.rotation
    }

    override operator fun plus(offset: Vector2f) {
        data.position.x += floor(offset.x.toDouble()).toFloat()
        data.position.y += floor(offset.y.toDouble()).toFloat()
    }

    override fun minus(offset: Vector2f) {
        data.position.x -= floor(offset.x.toDouble()).toFloat()
        data.position.y -= floor(offset.y.toDouble()).toFloat()
    }

    override fun move(dx: Float, dy: Float) {
        data.position.x -= floor(dx.toDouble()).toFloat()
        data.position.y -= floor(dy.toDouble()).toFloat()
    }
}



