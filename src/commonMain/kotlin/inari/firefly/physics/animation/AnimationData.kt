package com.inari.firefly.physics.animation

import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.Control
import com.inari.util.*
import com.inari.util.geom.*
import kotlin.jvm.JvmField

interface AnimatedDataBuilder<D : AnimatedData> {
    fun create(): D
}

abstract class AnimatedData {

    @JvmField var duration = 0L
    @JvmField var normalizedTime = 0f
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var resetOnFinish = true
    @JvmField var inversed = false
    @JvmField var callback: () -> Unit = VOID_CALL

    @JvmField val animationController = CReference(Control)

    abstract fun activate(entityIndex: Int)
}

@ComponentDSL
class EasedFloatAnimation private constructor() : AnimatedData() {

    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var easing: EasingFunction = Easing.LINEAR
    @JvmField var animatedProperty: (Int) -> FloatPropertyAccessor = { _ -> throw IllegalStateException() }

    internal lateinit var accessor: FloatPropertyAccessor

    companion object : AnimatedDataBuilder<EasedFloatAnimation> {
        override fun create() = EasedFloatAnimation()
    }

    override fun activate(entityIndex: Int) {
        accessor = animatedProperty(entityIndex)
    }
}

abstract class CurveAnimation protected constructor() : AnimatedData() {

    @JvmField var animatedXProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedYProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedRotationProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER

    internal lateinit var accessorX: FloatPropertyAccessor
    internal lateinit var accessorY: FloatPropertyAccessor
    internal lateinit var accessorRot: FloatPropertyAccessor

    override fun activate(entityIndex: Int) {
        accessorX = animatedXProperty(entityIndex)
        accessorY = animatedYProperty(entityIndex)
        accessorRot = animatedRotationProperty(entityIndex)
    }

}

@ComponentDSL
class BezierCurveAnimation private constructor() : CurveAnimation() {

    @JvmField var curve = CubicBezierCurve()
    @JvmField var easing: EasingFunction = Easing.LINEAR

    companion object : AnimatedDataBuilder<BezierCurveAnimation> {
        override fun create() = BezierCurveAnimation()
    }
}

@ComponentDSL
class BezierSplineAnimation private constructor() : CurveAnimation() {

    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }

    companion object : AnimatedDataBuilder<BezierSplineAnimation> {
        override fun create() = BezierSplineAnimation()
    }
}

@ComponentDSL
class IntFrameAnimation private constructor() : AnimatedData() {

    var timeline: Array<out IntFrame> = emptyArray()
        set(value) {
            field = value
            duration = field.fold(0L) { acc, frame -> acc + frame.timeInterval }
        }

    @JvmField var animatedProperty: (Int) -> IntPropertyAccessor = { _ -> throw IllegalStateException() }

    internal lateinit var accessor: IntPropertyAccessor

    override fun activate(entityIndex: Int) {
        accessor = animatedProperty(entityIndex)
    }

    companion object : AnimatedDataBuilder<IntFrameAnimation> {
        override fun create() = IntFrameAnimation()
    }

    interface IntFrame {
        val timeInterval: Long
        val value: Int
    }

}

