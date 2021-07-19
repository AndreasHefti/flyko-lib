package com.inari.firefly.physics.animation.timeline

import com.inari.firefly.FFContext
import kotlin.jvm.JvmField

internal class IntTimelineData {

    @JvmField var timeline: Array<out Frame.IntFrame> = emptyArray()
    @JvmField var startValue = 0
    @JvmField var endValue = -1
    @JvmField var inverseOnLoop = false

    @JvmField var currentFrameTime: Long = 0
    @JvmField var currentIndex = startValue

    fun reset() {
        currentFrameTime = 0
        currentIndex = startValue
    }

    fun update(looping: Boolean): Boolean {
        val frame = timeline[currentIndex]

        currentFrameTime += FFContext.timer.timeElapsed

        if (currentFrameTime > frame.timeInterval) {
            currentIndex++
            currentFrameTime = 0
            if ((endValue >= 0 && currentIndex > endValue) || currentIndex > timeline.size - 1) {
                if (looping) {
                    if (inverseOnLoop) {
                        val tmp = startValue
                        startValue = endValue
                        endValue = tmp
                    }
                    reset()
                    return true
                } else
                    return false
            }
        }

        return true
    }

}