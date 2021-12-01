package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.util.geom.*
import com.inari.util.geom.GeomUtils.bezierCurveAngleX
import com.inari.util.geom.GeomUtils.bezierCurvePoint
import kotlin.jvm.JvmField

class BezierSpline {

    var splineDuration: Long = 0
        private set
    private val curves = mutableListOf<BezierSplineSegment>()

    fun add(segment: BezierSplineSegment) {
        curves.add(segment)
        splineDuration += segment.duration
        calcRanges()
    }

    private fun calcRanges() {

        var lastToNormalized = 0f
        curves.forEach {
            val from = lastToNormalized
            lastToNormalized += 1f * it.duration /splineDuration
            it.segmentTimeRange = NormalizedTimeRange(from, lastToNormalized)
        }
    }

    fun getAtNormalized(time: Float): BezierSplineSegment =
        curves.find { it.segmentTimeRange.contains(time) } ?: throw RuntimeException("No BezierSplineSegment found for normalized time: $time")

}

class BezierSplineSegment(
    @JvmField val duration: Long,
    @JvmField val curve: CubicBezierCurve,
    @JvmField var easing: EasingFunction = Easing.LINEAR
) {
    constructor(duration: Long, p0: Vector2f, p1: Vector2f, p2: Vector2f, p3: Vector2f, easing: EasingFunction = Easing.LINEAR) :
            this(duration, CubicBezierCurve(p0, p1, p2, p3), easing)

    var segmentTimeRange: NormalizedTimeRange = NormalizedTimeRange()
        internal set
}

class BezierSplineAnimation private constructor() : TypedAnimation<ETransform>() {

    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }

    override fun update(timeStep: Float, data: AnimatedObjectData<ETransform>) {
        val transform = data.getProperty()
        if (applyTimeStep(timeStep, data)) {
            if (data.inversed) {
                val normTime = 1f - data.normalizedTime
                val curveSegment = spline.getAtNormalized(normTime)
                val segmentNormTime = GeomUtils.transformRange(normTime, curveSegment.segmentTimeRange.to, curveSegment.segmentTimeRange.from)
                val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime), true)
                transform.position(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime), true))
            } else {
                val curveSegment = spline.getAtNormalized(data.normalizedTime)
                val segmentNormTime = GeomUtils.transformRange(data.normalizedTime, curveSegment.segmentTimeRange.from, curveSegment.segmentTimeRange.to)
                val pos = bezierCurvePoint(curveSegment.curve, curveSegment.easing(segmentNormTime))
                transform.position(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, curveSegment.easing(segmentNormTime)))
            }
        } else {
            if (data.resetOnFinish) {
                spline.getAtNormalized(0f).curve.also {
                    transform.position(it.p0)
                    transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(it, 0f))
                }
            }
            dispose(data)
            data.callback()
        }
    }

    override fun componentType() = Animation
    companion object : SystemComponentSubType<Animation, BezierSplineAnimation>(Animation, BezierSplineAnimation::class) {
        override fun createEmpty() = BezierSplineAnimation()
    }
}