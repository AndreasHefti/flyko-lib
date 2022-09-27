package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.util.collection.DynArray
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurveAngleX
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurvePoint
import com.inari.util.geom.GeomUtils

abstract class Animation<D : AnimatedData>(
    protected val animatedData: DynArray<D>
) : Control() {

    override fun load() {
        super.load()
        Entity.registerComponentListener(::entityListener)
    }

    override fun dispose() {
        Entity.disposeComponentListener(::entityListener)
        super.dispose()
    }

    override fun update() = animatedData.forEach {
        if (it.active)
            update(it)
        else if (it.condition(it))
            it.active = true
    }

    protected abstract fun update(data: D)
    protected abstract fun accept(data: AnimatedData): D?

    private fun entityListener(key: ComponentKey, type: ComponentEventType) {
        if (type == ComponentEventType.ACTIVATED) notifyActivation(Entity[key])
        else if (type == ComponentEventType.DEACTIVATED) notifyDeactivation(Entity[key])
    }

    private fun notifyActivation(entity: Entity) {
        if (EAnimation !in entity.aspects) return
        val eAnimation = entity[EAnimation]
        eAnimation.animations.forEach { iAccept(it)?.let {
                data -> animatedData + data
        } }
    }

    private fun notifyDeactivation(entity: Entity) {
        if (EAnimation !in entity.aspects) return
        val eAnimation = entity[EAnimation]
        eAnimation.animations.forEach { accept(it)?.let {
                data -> animatedData - data
        } }
    }

    private fun iAccept(data: AnimatedData): D? {
        if ( data.animationController.targetKey.instanceIndex >= 0 &&
            data.animationController.targetKey.instanceIndex != this.index)
            return null
        return accept(data)
    }

}

object DefaultFloatEasing : Animation<EasedFloatAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this, true)
        Control.activate(this.name)
    }

    override fun update(data: EasedFloatAnimation) {
        val timeStep = Engine.timer.timeElapsed.toFloat() / data.duration
        if (data.applyTimeStep(timeStep))
            // calc and apply eased value
            if (data.inversed)
                data.accessor(GeomUtils.lerp(data.endValue, data.startValue, data.easing(data.normalizedTime)))
            else
                data.accessor(GeomUtils.lerp(data.startValue, data.endValue, data.easing(data.normalizedTime)))
    }

    override fun accept(data: AnimatedData): EasedFloatAnimation? =
        if (data !is EasedFloatAnimation) null
        else data
}

object BezierCurveAnimationControl: Animation<BezierCurveAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this, true)
        Control.activate(this.name)
    }

    override fun update(data: BezierCurveAnimation) {
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

    override fun accept(data: AnimatedData): BezierCurveAnimation? =
        if (data !is BezierCurveAnimation) null
        else data

}

object BezierSplineAnimationControl : Animation<BezierSplineAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this, true)
        Control.activate(this.name)
    }

    override fun update(data: BezierSplineAnimation) {
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

    override fun accept(data: AnimatedData): BezierSplineAnimation? =
        if (data !is BezierSplineAnimation) null
        else data
}

object IntFrameAnimationControl : Animation<IntFrameAnimation>(DynArray.of(5, 10)) {

    init {
        Control.registerAsSingleton(this, true)
        Control.activate(this.name)
    }

    override fun update(data: IntFrameAnimation) {
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

    override fun accept(data: AnimatedData): IntFrameAnimation? =
        if (data !is IntFrameAnimation) null
        else data

}