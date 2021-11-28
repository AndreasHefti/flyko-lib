package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.util.geom.*
import kotlin.jvm.JvmField

class BezierAnimation private constructor() : TypedAnimation<ETransform>() {

    @JvmField var curve = CubicBezierCurve()
    @JvmField var easing: EasingFunction = Easing.LINEAR

    override fun update(timeStep: Float, data: AnimatedObjectData<ETransform>) {
        val transform = data.getProperty()
        if (applyTimeStep(timeStep, data))
            if (data.inversed) {
                val pos = bezierCurvePoint(curve, easing(data.normalizedTime), true)
                transform.position(pos)
                //transform.pivot(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curve, easing(data.normalizedTime), true))
            } else {
                val pos = bezierCurvePoint(curve, easing(data.normalizedTime))
                transform.position(pos)
                //transform.pivot(pos)
                transform.rotation = GeomUtils.radToDeg(bezierCurveAngleX(curve, easing(data.normalizedTime)))
            }
        else {
            if (data.resetOnFinish) {
                transform.position(curve.p0)
                //transform.pivot(curve.p0)
                transform.rotation = 0f
            }
            dispose(data)
            data.callback()
        }
    }

    override fun componentType() = Animation
    companion object : SystemComponentSubType<Animation, BezierAnimation>(Animation, BezierAnimation::class) {
        override fun createEmpty() = BezierAnimation()
    }
}