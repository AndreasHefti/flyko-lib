package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.*
import com.inari.util.geom.*
import kotlin.jvm.JvmField

interface AnimatedDataBuilder<D : AnimatedData> {
    fun create(): D
}

abstract class AnimatedData {

    var entityIndex: EntityIndex = NULL_COMPONENT_INDEX
        internal set
    val paused: Boolean
        get() = entityIndex != NULL_COMPONENT_INDEX && active && Pausing.isPaused(Entity[entityIndex].groups)
    var active = false
        internal set
    var finished = false
        internal set

    @JvmField var duration = 0L
    @JvmField var normalizedTime = 0f
    @JvmField var suspend = false
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var resetOnFinish = true
    @JvmField var inversed = false
    @JvmField var nextAnimation: AnimatedData? = null
    @JvmField var condition: (AnimatedData) -> Boolean = { !finished }
    @JvmField var callback: () -> Unit = VOID_CALL
    @JvmField val animationController = CReference(Control)

    fun <AD : AnimatedData> withNextAnimation(builder: AnimatedDataBuilder<AD>, configure: AD.() -> Unit) {
        val result = builder.create()
        result.also(configure)
        nextAnimation = result
    }

    internal fun init(entityIndex: Int) {
        this.entityIndex = entityIndex
        initialize()
    }

    internal fun applyTimeStep(timeStep: Float): Boolean {
        normalizedTime += timeStep
        if (normalizedTime >= 1.0f) {
            normalizedTime = 0.0f
            if (suspend || !looping) {
                finish()
                nextAnimation?.active = true
                return false
            } else {
                if (inverseOnLoop)
                    inversed = !inversed
            }
        }
        return true
    }

    private fun finish() {
        active = false
        finished = true
        if (resetOnFinish)
            reset()
        callback()
    }

    abstract fun initialize()
    protected abstract fun reset()
}

@ComponentDSL
class EasedFloatData private constructor() : AnimatedData() {

    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var easing: EasingFunction = Easing.LINEAR
    @JvmField var animatedProperty: (Int) -> FloatPropertyAccessor = { _ -> throw IllegalStateException() }

    internal lateinit var accessor: FloatPropertyAccessor

    companion object : AnimatedDataBuilder<EasedFloatData> {
        override fun create() = EasedFloatData()
    }

    override fun initialize() {
        accessor = animatedProperty(entityIndex)
    }

    override fun reset() = accessor(startValue)

}

abstract class CurveData protected constructor() : AnimatedData() {

    @JvmField var animatedXProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedYProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedRotationProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER

    internal lateinit var accessorX: FloatPropertyAccessor
    internal lateinit var accessorY: FloatPropertyAccessor
    internal lateinit var accessorRot: FloatPropertyAccessor

    override fun initialize() {
        accessorX = animatedXProperty(entityIndex)
        accessorY = animatedYProperty(entityIndex)
        accessorRot = animatedRotationProperty(entityIndex)
    }

}

@ComponentDSL
class BezierCurveData private constructor() : CurveData() {

    @JvmField var curve = CubicBezierCurve()
    @JvmField var easing: EasingFunction = Easing.LINEAR

    override fun reset() {
        accessorX(curve.p0.x)
        accessorY(curve.p0.y)
        accessorRot(ZERO_FLOAT)
    }

    companion object : AnimatedDataBuilder<BezierCurveData> {
        override fun create() = BezierCurveData()
    }
}

@ComponentDSL
class BezierSplineData private constructor() : CurveData() {

    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }

    companion object : AnimatedDataBuilder<BezierSplineData> {
        override fun create() = BezierSplineData()
    }

    override fun reset() {
        spline.getAtNormalized(ZERO_FLOAT).curve.also {
            accessorX(it.p0.x)
            accessorY(it.p0.y)
            accessorRot(GeomUtils.radToDeg(CubicBezierCurve.bezierCurveAngleX(it, ZERO_FLOAT)))
        }
    }
}

@ComponentDSL
class IntFrameData private constructor() : AnimatedData() {

    var timeline: Array<out IntFrame> = emptyArray()
        set(value) {
            field = value
            duration = field.fold(0L) { acc, frame -> acc + frame.timeInterval }
        }

    @JvmField var animatedProperty: (Int) -> IntPropertyAccessor = { _ -> throw IllegalStateException() }

    internal lateinit var accessor: IntPropertyAccessor

    override fun initialize() {
        accessor = animatedProperty(entityIndex)
    }

    override fun reset() = accessor(timeline[0].value)

    companion object : AnimatedDataBuilder<IntFrameData> {
        override fun create() = IntFrameData()
    }

    interface IntFrame {
        val timeInterval: Long
        val value: Int
    }

}

