package com.inari.firefly.physics.animation.timeline

import com.inari.firefly.FFContext
import com.inari.firefly.NULL_CALL
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.property.IntPropertyAccessor
import com.inari.firefly.physics.animation.Animation
import com.inari.firefly.physics.animation.IntAnimation
import com.inari.firefly.physics.animation.entity.EntityPropertyAnimation
import kotlin.jvm.JvmField

class IntTimelineProperty private constructor() : EntityPropertyAnimation(), IntAnimation {

    @JvmField internal var propertyAccessor: IntPropertyAccessor? = null
    @JvmField internal val data = IntTimelineData()

    var timeline: Array<out Frame.IntFrame>
        get() = data.timeline
        set(value) { data.timeline = value }
    var startValue: Int
        get() = data.startValue
        set(value) { data.startValue = value }
    var endValue: Int
        get() = data.endValue
        set(value) { data.endValue = value }
    var inverseOnLoop: Boolean
        get() = data.inverseOnLoop
        set(value) { data.inverseOnLoop = value }

    override val value: Int
        get() = propertyAccessor?.get() ?: -1

    override fun applyToEntity(entity: Entity) {
        propertyAccessor = propertyRef.accessor(entity) as IntPropertyAccessor
    }

    override fun detachFromEntity(entity: Entity) {
        propertyAccessor = null
        reset()
        FFContext.deactivate(this)
    }

    override fun update() {
        if (data.update(looping))
            propertyAccessor?.set(data.timeline[data.currentIndex].value)
        else {
            if (resetOnFinish)
                reset()
            FFContext.deactivate(this)
            if (callback != NULL_CALL)
                callback()
        }

    }

    override fun reset() {
        data.reset()
        propertyAccessor?.set(data.timeline[data.currentIndex].value)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Animation, IntTimelineProperty>(Animation, IntTimelineProperty::class) {
        override fun createEmpty() = IntTimelineProperty()
    }

}