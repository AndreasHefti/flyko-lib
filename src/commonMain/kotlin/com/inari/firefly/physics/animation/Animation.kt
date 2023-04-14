package com.inari.firefly.physics.animation

import com.inari.firefly.core.*
import com.inari.util.collection.DynArray
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurveAngleX
import com.inari.util.geom.CubicBezierCurve.Companion.bezierCurvePoint
import com.inari.util.geom.GeomUtils

abstract class Animation<D : AnimatedData>(
    private val animatedData: DynArray<D>
) : Control() {

    private val entityListener: (ComponentKey, ComponentEventType) -> Unit = { key, type ->
        if (type == ComponentEventType.ACTIVATED) notifyActivation(Entity[key])
        else if (type == ComponentEventType.DEACTIVATED) notifyDeactivation(Entity[key])
    }

    override fun load() {
        super.load()
        Entity.registerComponentListener(entityListener)
    }

    override fun dispose() {
        Entity.disposeComponentListener(entityListener)
        super.dispose()
    }

    override fun update() {
        val iter = animatedData.iterator()
        if (Pausing.paused)
            while (iter.hasNext()) {
                val it = iter.next()
                if (it.paused) continue

                if (it.active)
                    update(it)
                else if (it.condition(it))
                    it.active = true
            }
        else
            while (iter.hasNext()) {
                val it = iter.next()
                if (it.active)
                    update(it)
                else if (it.condition(it))
                    it.active = true
            }
    }

    protected abstract fun update(data: D)
    protected abstract fun accept(data: AnimatedData): D?

    private fun notifyActivation(entity: Entity) {
        if (EAnimation !in entity.aspects) return
        val eAnimation = entity[EAnimation]
        val iter = eAnimation.animations.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            val data = iAccept(it)
            if (data != null) animatedData.add(data)
        }
    }

    private fun notifyDeactivation(entity: Entity) {
        if (EAnimation !in entity.aspects) return
        val eAnimation = entity[EAnimation]
        val iter = eAnimation.animations.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            val data = iAccept(it)
            if (data != null) animatedData.remove(data)
        }
    }

    private fun iAccept(data: AnimatedData): D? {
        if ( data.animationController.targetKey.componentIndex >= 0 &&
            data.animationController.targetKey.componentIndex != this.index)
            return null
        return accept(data)
    }
}

object FloatEasingAnimation: Animation<EasedFloatData>(DynArray.of(5, 10)) {

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.name)
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

    override fun accept(data: AnimatedData): EasedFloatData? =
        if (data !is EasedFloatData) null
        else data


}

object BezierCurveAnimation: Animation<BezierCurveData>(DynArray.of(5, 10)) {

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.name)
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

    override fun accept(data: AnimatedData): BezierCurveData? =
        if (data !is BezierCurveData) null
        else data
}

object BezierSplineAnimation : Animation<BezierSplineData>(DynArray.of(5, 10)) {

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.name)
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

    override fun accept(data: AnimatedData): BezierSplineData? =
        if (data !is BezierSplineData) null
        else data
}

object IntFrameAnimation : Animation<IntFrameData>(DynArray.of(5, 10)) {

    init {
        @Suppress("LeakingThis") registerStatic(this)
        Control.activate(this.name)
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

    override fun accept(data: AnimatedData): IntFrameData? =
        if (data !is IntFrameData) null
        else data
}