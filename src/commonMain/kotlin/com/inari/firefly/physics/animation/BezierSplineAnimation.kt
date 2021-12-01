package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.util.geom.*
import kotlin.jvm.JvmField

class BezierSplineAnimation private constructor() : TypedAnimation<ETransform>() {

    var spline = BezierSpline()
        set(value) {
            field = value
            duration = value.splineDuration
        }
    @JvmField var easing: EasingFunction = Easing.LINEAR

    override fun update(timeStep: Float, data: AnimatedObjectData<ETransform>) {
        val transform = data.getProperty()
        if (applyTimeStep(timeStep, data)) {
            if (data.inversed) {
                val normTime = 1f - data.normalizedTime
                val curveSegment = spline.getAtNormalized(normTime)
                val segmentNormTime = GeomUtils.transformRange(normTime, curveSegment.segmentTimeRange.to, curveSegment.segmentTimeRange.from)
                val pos = bezierCurvePoint(curveSegment.curve, easing(segmentNormTime), true)
                transform.position(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, easing(segmentNormTime), true))
            } else {
                val curveSegment = spline.getAtNormalized(data.normalizedTime)
                val segmentNormTime = GeomUtils.transformRange(data.normalizedTime, curveSegment.segmentTimeRange.from, curveSegment.segmentTimeRange.to)
                val pos = bezierCurvePoint(curveSegment.curve, easing(segmentNormTime))
                transform.position(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curveSegment.curve, easing(segmentNormTime)))
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