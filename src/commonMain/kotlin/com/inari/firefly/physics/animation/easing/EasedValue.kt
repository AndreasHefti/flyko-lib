package com.inari.firefly.physics.animation.easing

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.physics.animation.Animation
import com.inari.firefly.physics.animation.FloatAnimation
import com.inari.util.geom.Easing

class EasedValue : Animation(), FloatAnimation {

    private var control = EasingControl(this)

    var easing: Easing.EasingFunctions.EasingFunction
        get() = control.easing
        set(value) { control.easing = value }
    var startValue: Float
        get() = control.startValue
        set(value) { control.startValue = value }
    var endValue: Float
        get() = control.endValue
        set(value) { control.endValue = value }
    var duration: Long
        get() = control.duration
        set(value) { control.duration = value }
    var inverseOnLoop: Boolean
        get() = control.inverseOnLoop
        set(value) { control.inverseOnLoop = value }
    var propertyAccessor: FloatPropertyAccessor
        get() = control.propertyAccessor
        set(value) { control.propertyAccessor = value }

    override val value: Float
        get() = control.propertyAccessor.get()

    override fun reset() = control.reset()
    override fun update() = control.update()

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Animation, EasedValue>(Animation, EasedValue::class) {
        override fun createEmpty() = EasedValue()
    }
}