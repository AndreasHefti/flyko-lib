package com.inari.firefly.physics.animation.easing

import com.inari.firefly.FFContext
import com.inari.firefly.NULL_CALL
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.physics.animation.Animation
import com.inari.util.geom.Easing
import kotlin.jvm.JvmField
import kotlin.math.abs

internal class EasingControl(
    val animation: Animation
) {

    @JvmField var propertyAccessor: FloatPropertyAccessor = object : FloatPropertyAccessor {
        private var v = 0f
        override fun set(value: Float) { v = value}
        override fun get(): Float = v
    }

    @JvmField var easing: Easing.EasingFunctions.EasingFunction = Easing.Type.LINEAR
    @JvmField var startValue = 0f
    @JvmField var endValue = 0f
    @JvmField var duration: Long = 0
    @JvmField var inverseOnLoop = false

    @JvmField var inverse = false
    @JvmField var changeInValue = 0f
    @JvmField var runningTime: Long = 0

    internal fun init() {
        propertyAccessor = object : FloatPropertyAccessor {
            private var v = 0f
            override fun set(value: Float) { v = value}
            override fun get(): Float = v
        }
        reset()
    }

    fun reset() {
        runningTime = 0
        changeInValue  = endValue - startValue
        if (changeInValue < 0) {
            inverse = true
            changeInValue = abs(changeInValue)
        } else
            inverse = false

        propertyAccessor.set(startValue)
    }

    fun update() {
        runningTime += FFContext.timer.timeElapsed
        if (runningTime > duration) {
            if (animation.looping) {
                if (inverseOnLoop) {
                    val tmp = startValue
                    startValue = endValue
                    endValue = tmp
                }
                reset()
            } else {
                if (animation.resetOnFinish)
                    reset()
                FFContext.deactivate(animation)
                if (animation.callback != NULL_CALL)
                    animation.callback()
            }
            return
        }

        val t: Float = runningTime.toFloat() / duration
        var value = changeInValue * easing(t)
        if (inverse)
            value *= -1

        propertyAccessor.set(startValue + value)
    }

}