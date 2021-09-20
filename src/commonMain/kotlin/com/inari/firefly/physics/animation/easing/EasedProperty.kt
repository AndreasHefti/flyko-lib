package com.inari.firefly.physics.animation.easing

import com.inari.firefly.FFContext
import com.inari.firefly.NO_PROPERTY_REF
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.physics.animation.Animation
import com.inari.firefly.physics.animation.FloatAnimation
import com.inari.firefly.physics.animation.entity.EntityPropertyAnimation
import com.inari.util.geom.Easing

class EasedProperty : EntityPropertyAnimation(), FloatAnimation {

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

    override val value: Float
        get() = control.propertyAccessor.get()

    override fun applyToEntity(entity: Entity) {
        if (propertyRef == NO_PROPERTY_REF)
            throw IllegalStateException("No property reference for animation is set")
        control.propertyAccessor = propertyRef.accessor(entity) as FloatPropertyAccessor
        reset()
    }

    override fun detachFromEntity(entity: Entity) {
        control.init()
        reset()
        FFContext.deactivate(this)
    }

    override fun reset() = control.reset()


    override fun update() = control.update()

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Animation, EasedProperty>(Animation, EasedProperty::class) {
        override fun createEmpty() = EasedProperty()
    }
}