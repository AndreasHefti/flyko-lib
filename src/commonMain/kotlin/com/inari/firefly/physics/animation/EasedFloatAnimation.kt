package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Easing
import com.inari.util.geom.GeomUtils
import kotlin.jvm.JvmField

class EasedFloatAnimation private constructor() : TypedAnimation<Float>() {

    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var easing: Easing.EasingFunctions.EasingFunction = Easing.Type.LINEAR

    override fun update(timeStep: Float, data: AnimatedObjectData<Float>) {
        if (applyTimeStep(timeStep, data))
            // calc and apply eased value
            if (data.inversed)
                data.setProperty(GeomUtils.lerp(endValue, startValue, easing(data.normalizedTime)))
            else
                data.setProperty(GeomUtils.lerp(startValue, endValue, easing(data.normalizedTime)))
        else {
            // animation finished
            if (data.resetOnFinish)
                data.setProperty(startValue)
            dispose(data)
            data.callback()
        }
    }

    override fun componentType() = Animation
    companion object : SystemComponentSubType<Animation, EasedFloatAnimation>(Animation, EasedFloatAnimation::class) {
        override fun createEmpty() = EasedFloatAnimation()
    }
}