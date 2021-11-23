package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Easing
import com.inari.util.geom.EasingFunction
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class EasedPositionAnimation private constructor() : TypedAnimation<Vector2f>() {

    @JvmField var startValue = Vector2f()
    @JvmField var endValue = Vector2f()
    @JvmField var easing: EasingFunction = Easing.LINEAR

    override fun update(timeStep: Float, data: AnimatedObjectData<Vector2f>) {
        if (applyTimeStep(timeStep, data))
            // calc and apply eased value
            if (data.inversed)
                GeomUtils.lerp(endValue, startValue, easing(data.normalizedTime), data.getProperty())
            else
                GeomUtils.lerp(startValue, endValue, easing(data.normalizedTime), data.getProperty())
        else {
            // animation finished
            if (data.resetOnFinish)
                data.setProperty(startValue)
            dispose(data)
            data.callback()
        }
    }

    override fun componentType() = Animation
    companion object : SystemComponentSubType<Animation, EasedPositionAnimation>(Animation, EasedPositionAnimation::class) {
        override fun createEmpty() = EasedPositionAnimation()
    }
}