package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.util.collection.DynArray
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurveAngleX
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurvePoint
import com.inari.util.geom.GeomUtils
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object AnimationSystem : Control() {

    @JvmField internal val animations = DynArray.of<AnimatedData<*>>(2, 5)

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.index)
    }

    override fun update() {
        val iter = animations.iterator()
        val paused = Pausing.paused
        while (iter.hasNext()) {
            val it = iter.next()
            if (paused && it.paused) continue

            it.update()
        }
    }
}

abstract class AnimationIntegrator<D : AnimatedData<D>> : Component(AnimationIntegrator) {

    internal operator fun invoke(data: D) = update(data)
    protected abstract fun update(data: D)

    companion object : AbstractComponentSystem<AnimationIntegrator<*>>("AnimationIntegrator") {
        override fun allocateArray(size: Int): Array<AnimationIntegrator<*>?> = arrayOfNulls(size)
    }
}

object FloatEasingAnimation: AnimationIntegrator<EasedFloatData>() {

    init {
        @Suppress("LeakingThis") registerStatic(this)
    }

    override fun update(data: EasedFloatData) {
        val timeStep: Float = 1f * Engine.timer.timeElapsed / data.duration
        if (data.applyTimeStep(timeStep))
            // calc and apply eased value
            if (data.inversed)
                data.accessor(GeomUtils.lerp(data.endValue, data.startValue, data.easing(data.normalizedTime)))
            else
                data.accessor(GeomUtils.lerp(data.startValue, data.endValue, data.easing(data.normalizedTime)))
    }
}

object BezierCurveAnimation: AnimationIntegrator<BezierCurveData>() {

    init {
        @Suppress("LeakingThis") registerStatic(this)
    }

    override fun update(data: BezierCurveData) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (data.applyTimeStep(timeStep))
            if (data.inversed) {
                val pos = bezierCurvePoint(data.curve, data.easing(data.normalizedTime), true)
                data.accessorX(pos.x)
                data.accessorY(pos.y)
                data.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(data.curve, data.easing(data.normalizedTime), true)))
            } else {
                val pos = bezierCurvePoint(data.curve, data.easing(data.normalizedTime))
                data.accessorX(pos.x)
                data.accessorY(pos.y)
                data.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(data.curve, data.easing(data.normalizedTime))))
            }
    }
}

object BezierSplineAnimation : AnimationIntegrator<BezierSplineData>() {

    init {
        @Suppress("LeakingThis") registerStatic(this)
    }

    override fun update(data: BezierSplineData) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (data.applyTimeStep(timeStep)) {
            if (data.inversed) {
                val normTime = 1f - data.normalizedTime
                val curveSegment = data.spline.getAtNormalized(normTime)
                val segmentNormTime = GeomUtils.transformRange(normTime, curveSegment.segmentTimeRange.to, curveSegment.segmentTimeRange.from)
                val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime), true)
                data.accessorX(pos.x)
                data.accessorY(pos.y)
                data.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime), true)))
            } else {
                val curveSegment = data.spline.getAtNormalized(data.normalizedTime)
                val segmentNormTime = GeomUtils.transformRange(data.normalizedTime, curveSegment.segmentTimeRange.from, curveSegment.segmentTimeRange.to)
                val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime))
                data.accessorX(pos.x)
                data.accessorY(pos.y)
                data.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime))))
            }
        }
    }
}

object IntFrameAnimation : AnimationIntegrator<IntFrameData>() {

    init {
        @Suppress("LeakingThis") registerStatic(this)
    }

    override fun update(data: IntFrameData) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (data.applyTimeStep(timeStep)) {
            var t = 0f
            var i = if (data.inversed) data.timeline.size else  -1
            while (t <= data.normalizedTime && i < data.timeline.size - 1) {
                if (data.inversed) i-- else i++
                t += data.timeline[i].timeInterval / data.duration.toFloat()
            }
            data.accessor(data.timeline[i].value)
        }
    }
}