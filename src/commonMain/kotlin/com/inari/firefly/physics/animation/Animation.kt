package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.graphics.sprite.SpriteFrame
import com.inari.util.*
import com.inari.util.collection.DynArray
import com.inari.util.geom.*
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurveAngleX
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurvePoint
import kotlin.jvm.JvmField

abstract class AnimationIntegrator<A : Animation> {
    abstract fun integrateStep(a: A)
}

abstract class Animation protected constructor(subtype: ComponentType<out Animation>) : Component(subtype) {

    @JvmField var duration = 0L
    @JvmField var normalizedTime = 0f
    @JvmField var suspend = false
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var inverse = false
    @JvmField var resetOnFinish = true
    @JvmField var disposeWhenNoRefs = true

    init {
        autoActivation = true
    }

    abstract fun register(index: ComponentIndex)
    protected abstract fun doDispose(index: ComponentIndex)
    protected abstract val hasReferences: Boolean
    protected abstract fun integrate()
    abstract fun reset()
    fun finish() {
        Animation.deactivate(this)
        if (resetOnFinish)
            reset()
    }

    fun dispose(index: ComponentIndex) {
        doDispose(index)
        if (disposeWhenNoRefs && !hasReferences)
            ComponentSystem.delete(this.key)
    }

    private val timer = Engine.timer
    protected fun update() {
        normalizedTime += 1f * timer.timeElapsed / duration
        if (normalizedTime >= 1.0f) {
            normalizedTime = 0.0f
            if (suspend || !looping) {
                finish()
                return
            } else if (inverseOnLoop)
                inverse = !inverse
        }

        integrate()
    }

    companion object : AbstractComponentSystem<Animation>("Animation") {
        override fun allocateArray(size: Int): Array<Animation?> = arrayOfNulls(size)

        init {
            Engine.registerListener(Engine.UPDATE_EVENT_TYPE, ::updateAllActiveAnimations)
        }

        private fun updateAllActiveAnimations() {
            val iter = Animation.activeIndexIterator()
            if (Pausing.paused)
                while (iter.hasNext()) {
                    val c = Animation[iter.nextInt()]
                    if (!Pausing.isPaused(c.groups))
                        c.update()
                }
            else
                while (iter.hasNext())
                    Animation[iter.nextInt()].update()
        }
    }
}

class EasedValueAnimation private constructor() : Animation(EasedValueAnimation) {

    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var easing: EasingFunction = Easing.LINEAR
    @JvmField var animatedProperty = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var integrator = EasingIntegrator

    @JvmField internal var accessor = VOID_FLOAT_PROPERTY_ACCESSOR
    private var multiAccessor = false

    override val hasReferences: Boolean
        get() = accessor != VOID_FLOAT_PROPERTY_ACCESSOR

    override fun register(index: ComponentIndex) {
        if (accessor == VOID_FLOAT_PROPERTY_ACCESSOR)
            accessor = animatedProperty(index)
        else if (multiAccessor)
            (accessor as FloatPropertyAccessorMultiplexer).accessorRefs.add(animatedProperty(index))
        else {
            val newAccessor = FloatPropertyAccessorMultiplexer()
            newAccessor.accessorRefs.add(accessor)
            newAccessor.accessorRefs.add(animatedProperty(index))
            accessor = newAccessor
            multiAccessor = true
        }
    }

    override fun doDispose(index: ComponentIndex) {
        if (multiAccessor) {
            val acc = accessor as FloatPropertyAccessorMultiplexer
            acc.accessorRefs.remove(animatedProperty(index))
            if (acc.accessorRefs.isEmpty) {
                accessor = VOID_FLOAT_PROPERTY_ACCESSOR
                multiAccessor = false
            }
         } else
            accessor = VOID_FLOAT_PROPERTY_ACCESSOR
    }

    override fun integrate() = integrator.integrateStep(this)
    override fun reset() = accessor(startValue)

    companion object : ComponentSubTypeBuilder<Animation, EasedValueAnimation>(Animation,"EasedValue") {
        override fun create() = EasedValueAnimation()
    }
}

class SpriteFrameAnimation private constructor() : Animation(SpriteFrameAnimation) {

    @JvmField var timeline: Array<out SpriteFrame> = emptyArray()
    @JvmField var animatedProperty = VOID_INT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var integrator = SpriteFrameIntegrator

    @JvmField internal var accessor = VOID_INT_PROPERTY_ACCESSOR
    private var multiAccessor = false

    override fun initialize() {
        super.initialize()
        for (i in timeline.indices)
            duration += timeline[i].interval
    }

    override val hasReferences: Boolean
        get() = accessor != VOID_INT_PROPERTY_ACCESSOR

    override fun register(index: ComponentIndex) {
        if (accessor == VOID_INT_PROPERTY_ACCESSOR)
            accessor = animatedProperty(index)
        else if (multiAccessor)
            (accessor as IntPropertyAccessorMultiplexer).accessorRefs.add(animatedProperty(index))
        else {
            val newAccessor = IntPropertyAccessorMultiplexer()
            newAccessor.accessorRefs.add(accessor)
            newAccessor.accessorRefs.add(animatedProperty(index))
            accessor = newAccessor
            multiAccessor = true
        }
    }

    override fun doDispose(index: ComponentIndex) {
        if (multiAccessor) {
            val acc = accessor as IntPropertyAccessorMultiplexer
            acc.accessorRefs.remove(animatedProperty(index))
            if (acc.accessorRefs.isEmpty) {
                accessor = VOID_INT_PROPERTY_ACCESSOR
                multiAccessor = false
            }
        } else if (accessor == animatedProperty(index))
            accessor = VOID_INT_PROPERTY_ACCESSOR
    }

    override fun integrate() = integrator.integrateStep(this)
    override fun reset() = accessor(timeline[0].sprite.spriteIndex)

    companion object : ComponentSubTypeBuilder<Animation, SpriteFrameAnimation>(Animation,"SpriteFrameAnimation") {
        override fun create() = SpriteFrameAnimation()
    }
}

abstract class CurveAnimation protected constructor(subtype: ComponentType<out CurveAnimation>) : Animation(subtype) {

    @JvmField var animatedXProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedYProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER
    @JvmField var animatedRotationProperty: (Int) -> FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER

    @JvmField internal var accessorX = VOID_FLOAT_PROPERTY_ACCESSOR
    @JvmField internal var accessorY = VOID_FLOAT_PROPERTY_ACCESSOR
    @JvmField internal var accessorRot = VOID_FLOAT_PROPERTY_ACCESSOR

    override fun register(index: ComponentIndex) {
        accessorX = animatedXProperty(index)
        accessorY = animatedYProperty(index)
        accessorRot = animatedRotationProperty(index)
    }

    override val hasReferences: Boolean
        get() = accessorX != VOID_FLOAT_PROPERTY_ACCESSOR

    override fun doDispose(index: ComponentIndex) {
        if (accessorX == animatedXProperty(index))
            accessorX = VOID_FLOAT_PROPERTY_ACCESSOR
        if (accessorY == animatedYProperty(index))
            accessorY = VOID_FLOAT_PROPERTY_ACCESSOR
        if (accessorRot == animatedRotationProperty(index))
            accessorRot = VOID_FLOAT_PROPERTY_ACCESSOR
    }
}

class BezierCurveAnimation private constructor() : CurveAnimation(BezierCurveAnimation) {

    @JvmField var curve = CubicBezierCurve()
    @JvmField var easing: EasingFunction = Easing.LINEAR
    @JvmField var integrator = BezierCurveIntegrator

    override fun integrate()  = integrator.integrateStep(this)

    override fun reset() {
        accessorX(curve.p0.x)
        accessorY(curve.p0.y)
        accessorRot(ZERO_FLOAT)
    }

    companion object : ComponentSubTypeBuilder<Animation, BezierCurveAnimation>(Animation,"BezierCurveAnimation") {
        override fun create() = BezierCurveAnimation()
    }
}

class BezierSplineAnimation private constructor() : CurveAnimation(BezierSplineAnimation) {

    @JvmField var integrator = BezierSplineIntegrator
    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }

    override fun integrate() = integrator.integrateStep(this)

    override fun reset() {
        val c = spline.getAtNormalized(ZERO_FLOAT).curve
        accessorX(c.p0.x)
        accessorY(c.p0.y)
        accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(c, ZERO_FLOAT)))
    }

    companion object : ComponentSubTypeBuilder<Animation, BezierSplineAnimation>(Animation,"BezierSplineAnimation") {
        override fun create() = BezierSplineAnimation()
    }
}

object EasingIntegrator: AnimationIntegrator<EasedValueAnimation>() {

    override fun integrateStep(a: EasedValueAnimation) {
        // calc and apply eased value
        if (a.inverse)
            a.accessor(GeomUtils.lerp(a.endValue, a.startValue, a.easing(a.normalizedTime)))
        else
            a.accessor(GeomUtils.lerp(a.startValue, a.endValue, a.easing(a.normalizedTime)))
    }
}

object BezierCurveIntegrator: AnimationIntegrator<BezierCurveAnimation>() {

    override fun integrateStep(a: BezierCurveAnimation) {
        if (a.inverse) {
            val pos = bezierCurvePoint(a.curve, a.easing(a.normalizedTime), true)
            a.accessorX(pos.x)
            a.accessorY(pos.y)
            a.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(a.curve, a.easing(a.normalizedTime), true)))
        } else {
            val pos = bezierCurvePoint(a.curve, a.easing(a.normalizedTime))
            a.accessorX(pos.x)
            a.accessorY(pos.y)
            a.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(a.curve, a.easing(a.normalizedTime))))
        }
    }
}

object BezierSplineIntegrator : AnimationIntegrator<BezierSplineAnimation>() {

    override fun integrateStep(a: BezierSplineAnimation) {
        if (a.inverse) {
            val normTime = 1f - a.normalizedTime
            val curveSegment = a.spline.getAtNormalized(normTime)
            val segmentNormTime = GeomUtils.transformRange(normTime, curveSegment.segmentTimeRange.to, curveSegment.segmentTimeRange.from)
            val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime), true)
            a.accessorX(pos.x)
            a.accessorY(pos.y)
            a.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime), true)))
        } else {
            val curveSegment = a.spline.getAtNormalized(a.normalizedTime)
            val segmentNormTime = GeomUtils.transformRange(a.normalizedTime, curveSegment.segmentTimeRange.from, curveSegment.segmentTimeRange.to)
            val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime))
            a.accessorX(pos.x)
            a.accessorY(pos.y)
            a.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime))))
        }
    }
}

object SpriteFrameIntegrator : AnimationIntegrator<SpriteFrameAnimation>() {

    override fun integrateStep(a: SpriteFrameAnimation) {
        var t = 0f
        var i = if (a.inverse) a.timeline.size else  -1
        while (t <= a.normalizedTime && i < a.timeline.size - 1) {
            if (a.inverse) i-- else i++
            t += a.timeline[i].interval / a.duration.toFloat()
        }
        a.accessor(a.timeline[i].sprite.spriteIndex)
    }
}

class FloatPropertyAccessorMultiplexer(
    initSize: Int = 10,
    grow: Int = 20
) : FloatPropertyAccessor {

    @JvmField val accessorRefs = DynArray.of<FloatPropertyAccessor>(initSize, grow)

    override fun invoke(value: Float) {
        var i = accessorRefs.nextIndex(0)
        while (i >= ZERO_INT) {
            accessorRefs[i]?.invoke(value)
            i = accessorRefs.nextIndex(i)
        }
    }
}

class IntPropertyAccessorMultiplexer(
    initSize: Int = 10,
    grow: Int = 20
) : IntPropertyAccessor {

    @JvmField val accessorRefs = DynArray.of<IntPropertyAccessor>(initSize, grow)

    override fun invoke(value: Int) {
        var i = accessorRefs.nextIndex(0)
        while (i >= ZERO_INT) {
            accessorRefs[i]?.invoke(value)
            i = accessorRefs.nextIndex(i)
        }
    }
}