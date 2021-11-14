package com.inari.firefly.physics.animation

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField
import kotlin.reflect.KMutableProperty0

typealias PropertyAccessor<T> = KMutableProperty0<T>
typealias PropertyRefResolver<T> = (CompId) -> PropertyAccessor<T>

abstract class Animation protected constructor() : SystemComponent(Animation::class.simpleName!!) {

    @JvmField var duration = 0L

    fun update() = update(FFContext.timer.timeElapsed.toFloat() / duration)
    abstract fun register(data: AnimatedObjectData<*>)
    abstract fun dispose(data: AnimatedObjectData<*>)
    abstract fun update(timeStep: Float)

    fun applyTimeStep(timeStep: Float, data: AnimatedObjectData<*>): Boolean {
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

    companion object : SystemComponentType<Animation>(Animation::class)
}

@Suppress("UNCHECKED_CAST")
abstract class TypedAnimation<T> protected constructor() : Animation() {

    private val animatedData = DynArray.of<AnimatedObjectData<T>>()

    override fun register(data: AnimatedObjectData<*>) {
        animatedData.add(data as AnimatedObjectData<T>)
    }

    override fun dispose(data: AnimatedObjectData<*>) {
        animatedData.remove(data as AnimatedObjectData<T>)
    }

    override fun update(timeStep: Float) {
        if (animatedData.isEmpty)
            return

        val i = animatedData.iterator()
        while (i.hasNext()) update(timeStep, i.next())
    }

    abstract fun update(timeStep: Float, data: AnimatedObjectData<T>)
}
