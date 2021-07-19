package com.inari.firefly.physics.animation.timeline

import com.inari.firefly.FFContext
import com.inari.firefly.NULL_CALL
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.property.IntPropertyAccessor
import com.inari.firefly.physics.animation.Animation
import com.inari.firefly.physics.animation.IntAnimation
import com.inari.firefly.physics.animation.entity.EntityPropertyAnimation
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class IntTimelineProperties private constructor() : EntityPropertyAnimation(), IntAnimation {

    @JvmField internal var propertyAccessor: DynArray<IntPropertyAccessor> = DynArray.of()
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
        get() = data.timeline[data.currentIndex].value

    override fun applyToEntity(entity: Entity) {
        propertyAccessor + propertyRef.accessor(entity) as IntPropertyAccessor
    }

    override fun detachFromEntity(entity: Entity) {
        propertyAccessor - propertyRef.accessor(entity) as IntPropertyAccessor
    }

    override fun update() {
        if (data.update(looping)) {
            var i = 0
            val v = value
            while (i < propertyAccessor.capacity) {
                val acc = propertyAccessor[i++] ?: continue
                acc.set(v)
            }
        } else {
            if (resetOnFinish)
                reset()
            FFContext.deactivate(this)
            if (callback != NULL_CALL)
                callback()
        }

    }

    override fun reset() {
        data.reset()
        var i = 0
        val v = value
        while (i < propertyAccessor.capacity) {
            val acc = propertyAccessor[i++] ?: continue
            acc.set(v)
        }
    }

    companion object : SystemComponentSubType<Animation, IntTimelineProperties>(Animation, IntTimelineProperties::class) {
        override fun createEmpty() = IntTimelineProperties()
    }

}