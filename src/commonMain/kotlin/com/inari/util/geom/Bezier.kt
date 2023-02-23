package com.inari.util.geom

import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField
import kotlin.math.pow

class CubicBezierCurve(
    @JvmField val p0: Vector2f = Vector2f(),
    @JvmField val p1: Vector2f = Vector2f(),
    @JvmField val p2: Vector2f = Vector2f(),
    @JvmField val p3: Vector2f = Vector2f()
) {
    companion object {

        private val tmpV0 = Vector2f()
        private val tmpV1 = Vector2f()
        private val tmpV2 = Vector2f()
        private val tmpV3 = Vector2f()

        fun bezierCurvePoint(curve: CubicBezierCurve, t: Float, invert: Boolean = false) =
            if (invert)
                bezierCurvePoint(curve.p3, curve.p2, curve.p1, curve.p0, t)
            else
                bezierCurvePoint(curve.p0, curve.p1, curve.p2, curve.p3, t)
        /** u = 1f - t
         *  t2 = t * t
         *  u2 = u * u
         *  u3 = u2 * u
         *  t3 = t2 * t
         *
         *  v(t) = (u3) * v0 + (3f * u2 * t) * v1 + (3f * u * t2) * v2 +(t3) * v3;
         **/
        fun bezierCurvePoint(v0: Vector2f, v1: Vector2f, v2: Vector2f, v3: Vector2f, t: Float): Vector2f {
            val u = 1f - t
            val t2 = t * t
            val u2 = u * u
            val u3 = u2 * u
            val t3 = t2 * t

            return tmpV0(v0) * u3 + tmpV1(v1) * (3f * u2 * t) + tmpV2(v2) * (3f * u * t2) + tmpV3(v3) * t3
        }

        fun bezierCurveAngleX(curve: CubicBezierCurve, t: Float, invert: Boolean = false) =
            if (invert)
                bezierCurveAngleX(curve.p3, curve.p2, curve.p1, curve.p0, t)
            else
                bezierCurveAngleX(curve.p0, curve.p1, curve.p2, curve.p3, t)
        /** v′(t)=(1−t)2 (v1−v0) + 2t(1−t) (v2−v1) + t2 (v3−v2)
         *  ax(rad) = atan2(v.y'(t), v.x'(t))
         */
        fun bezierCurveAngleX(v0: Vector2f, v1: Vector2f, v2: Vector2f, v3: Vector2f, t: Float): Float {
            tmpV0(v1) - v0
            tmpV1(v2) - v1
            tmpV2(v3) - v2

            return GeomUtils.angleX(tmpV3(tmpV0) * (1f - t).pow(2f) + tmpV1 * (2f * t * (1f - t)) + tmpV2 * t.pow(2f))
        }
    }
}

class BezierSpline {

    var splineDuration: Long = 0
        private set
    private val curves = DynArray.of<BezierSplineSegment>(5)

    fun add(segment: BezierSplineSegment) {
        curves.add(segment)
        splineDuration += segment.duration
        calcRanges()
    }

    private fun calcRanges() {
        var lastToNormalized = 0f
        val iter = curves.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
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