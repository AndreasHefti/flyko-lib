package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.*
import com.inari.util.geom.*
import kotlin.jvm.JvmField

interface AnimatedDataBuilder<D : AnimatedData<D>> {
    fun create(): D
}

abstract class AnimatedData<AD : AnimatedData<AD>> {

    var entityIndex: EntityIndex = NULL_COMPONENT_INDEX
        internal set
    val paused: Boolean
        get() = entityIndex != NULL_COMPONENT_INDEX && active && Pausing.isPaused(Entity[entityIndex].groups)

    @JvmField var active = false
    @JvmField var finished = false
    @JvmField var autoActivation = true
    @JvmField var duration = 0L
    @JvmField var normalizedTime = 0f
    @JvmField var suspend = false
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var resetOnFinish = true
    @JvmField var inversed = false
    @JvmField var nextAnimation: AnimatedData<*>? = null
    @JvmField var callback: () -> Unit = VOID_CALL
    abstract val integrator: AnimationIntegrator<AD>

    @Suppress("LeakingThis", "UNCHECKED_CAST")
    private val data: AD = this as AD

    internal fun init(entityIndex: Int) {
        this.entityIndex = entityIndex
        if (autoActivation)
            active = true
        initialize()
    }

    internal fun update() {
        if (active) integrator(data)
    }

    internal fun applyTimeStep(timeStep: Float): Boolean {
        normalizedTime += timeStep
        if (normalizedTime >= 1.0f) {
            normalizedTime = 0.0f
            if (suspend || !looping) {
                 finish()
                nextAnimation?.active = true
                return false
            } else if (inverseOnLoop)
                inversed = !inversed
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
class EasedFloatData private constructor() : AnimatedData<EasedFloatData>() {

    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var easing: EasingFunction = Easing.LINEAR
    @JvmField var animatedProperty: (Int) -> FloatPropertyAccessor = { _ -> throw IllegalStateException() }

    override var integrator = FloatEasingAnimation
    internal lateinit var accessor: FloatPropertyAccessor

    override fun initialize() {
        accessor = animatedProperty(entityIndex)
    }

    override fun reset() = accessor(startValue)

    companion object : AnimatedDataBuilder<EasedFloatData> {
        override fun create() = EasedFloatData()
    }
}

abstract class CurveData<D : AnimatedData<D>> protected constructor() : AnimatedData<D>() {

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
class BezierCurveData private constructor() : CurveData<BezierCurveData>() {

    @JvmField var curve = CubicBezierCurve()
    @JvmField var easing: EasingFunction = Easing.LINEAR

    override val integrator = BezierCurveAnimation

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
class BezierSplineData private constructor() : CurveData<BezierSplineData>() {

    override val integrator =  BezierSplineAnimation

    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }

    override fun reset() {
        val c = spline.getAtNormalized(ZERO_FLOAT).curve
        accessorX(c.p0.x)
        accessorY(c.p0.y)
        accessorRot(GeomUtils.radToDeg(CubicBezierCurve.bezierCurveAngleX(c, ZERO_FLOAT)))
    }

    companion object : AnimatedDataBuilder<BezierSplineData> {
        override fun create() = BezierSplineData()
    }
}

@ComponentDSL
class IntFrameData private constructor() : AnimatedData<IntFrameData>() {

    @JvmField var timeline: Array<out IntFrame> = emptyArray()
    @JvmField var animatedProperty: (Int) -> IntPropertyAccessor = { _ -> throw IllegalStateException() }

    override val integrator = IntFrameAnimation
    internal lateinit var accessor: IntPropertyAccessor

    override fun initialize() {
        accessor = animatedProperty(entityIndex)
        duration = 0L
        for (i in timeline.indices)
            duration += timeline[i].timeInterval
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

