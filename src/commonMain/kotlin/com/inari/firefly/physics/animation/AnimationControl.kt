package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.util.ZERO_FLOAT
import com.inari.util.collection.DynArray
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurveAngleX
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurvePoint
import com.inari.util.geom.GeomUtils

abstract class AnimationControl<D : AnimatedData>(
    protected val animatedData: DynArray<D>
) : ComponentControl<Entity>() {

    override val controlledComponentType = Entity

    override fun notifyActivation(component: Entity) {
        if (EAnimation !in component.components) return
        val eAnimation = component[EAnimation]
        eAnimation.animations.forEach { iAccept(it)?.let {
                data -> animatedData + data
        } }
    }

    override fun notifyDeactivation(component: Entity) {
        if (EAnimation !in component.components) return
        val eAnimation = component[EAnimation]
        eAnimation.animations.forEach { accept(it)?.let {
                data -> animatedData - data
        } }
    }

    protected fun applyTimeStep(timeStep: Float, data: AnimatedData): Boolean {
        data.normalizedTime += timeStep
        if (data.normalizedTime >= 1.0f) {
            data.normalizedTime = 0.0f
            // animation step finished
            if (data.looping) {
                if (data.inverseOnLoop)
                    data.inversed = !data.inversed
            } else
                return false
        }
        return true
    }

   private fun iAccept(data: AnimatedData): D? {
        if ( data.animationController.targetKey.instanceId >= 0 &&
                    data.animationController.targetKey.instanceId != this.index)
            return null
        return accept(data)
    }

    override fun update() =
        animatedData.forEach { update(it) }

    protected abstract fun update(data: D)
    protected abstract fun accept(data: AnimatedData): D?

}

object DefaultFloatEasingControl : AnimationControl<EasedFloatAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this)
        Control.activate(this.name)
    }

    override fun update(data: EasedFloatAnimation) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (applyTimeStep(timeStep, data))
            // calc and apply eased value
            if (data.inversed)
                data.accessor(GeomUtils.lerp(data.endValue, data.startValue, data.easing(data.normalizedTime)))
            else
                data.accessor(GeomUtils.lerp(data.startValue, data.endValue, data.easing(data.normalizedTime)))
        else {
            // animation finished
            if (data.resetOnFinish)
                data.accessor(data.startValue)
            animatedData.remove(data)
            data.callback()
        }
    }

    override fun accept(data: AnimatedData): EasedFloatAnimation? =
        if (data !is EasedFloatAnimation) null
        else data
}

object BezierCurveAnimationControl: AnimationControl<BezierCurveAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this)
        Control.activate(this.name)
    }

    override fun update(data: BezierCurveAnimation) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (applyTimeStep(timeStep, data))
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
        else {
            if (data.resetOnFinish) {
                data.accessorX(data.curve.p0.x)
                data.accessorY(data.curve.p0.y)
                data.accessorRot(ZERO_FLOAT)
            }
            animatedData.remove(data)
            data.callback()
        }
    }

    override fun accept(data: AnimatedData): BezierCurveAnimation? =
        if (data !is BezierCurveAnimation) null
        else data

}

object BezierSplineAnimationControl : AnimationControl<BezierSplineAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this)
        Control.activate(this.name)
    }

    override fun update(data: BezierSplineAnimation) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (applyTimeStep(timeStep, data)) {
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
        } else {
            if (data.resetOnFinish) {
                data.spline.getAtNormalized(ZERO_FLOAT).curve.also {
                    data.accessorX(it.p0.x)
                    data.accessorY(it.p0.y)
                    data.accessorRot(GeomUtils.radToDeg(bezierCurveAngleX(it, ZERO_FLOAT)))
                }
            }
            animatedData.remove(data)
            data.callback()
        }
    }

    override fun accept(data: AnimatedData): BezierSplineAnimation? =
        if (data !is BezierSplineAnimation) null
        else data
}

object IntFrameAnimationControl : AnimationControl<IntFrameAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this)
        Control.activate(this.name)
    }

    override fun update(data: IntFrameAnimation) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (applyTimeStep(timeStep, data)) {
            var t = 0f
            var i = if (data.inversed) data.timeline.size else  -1
            while (t <= data.normalizedTime && i < data.timeline.size - 1) {
                if (data.inversed) i-- else i++
                t += data.timeline[i].timeInterval / data.duration.toFloat()
            }
            data.accessor(data.timeline[i].value)
        } else {
            // animation finished
            if (data.resetOnFinish)
                data.accessor(data.timeline[0].value)
            animatedData.remove(data)
            data.callback()
        }
    }

    override fun accept(data: AnimatedData): IntFrameAnimation? =
        if (data !is IntFrameAnimation) null
        else data

}