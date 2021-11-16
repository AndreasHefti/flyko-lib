package com.inari.firefly.physics.animation

import com.inari.firefly.core.system.SystemComponentSubType

class TimelineIntAnimation private constructor() : TypedAnimation<Int>() {

    var timeline: Array<out TimelineFrame.IntFrame> = emptyArray()
        set(value) {
            field = value
            duration = field.fold(0L) { acc, frame -> acc + frame.timeInterval }
        }

    override fun update(timeStep: Float, data: AnimatedObjectData<Int>) {
        if (applyTimeStep(timeStep, data)) {
            var t = 0f
            var i = if (data.inversed) timeline.size else  -1
            while (t <= data.normalizedTime && i < timeline.size - 1) {
                if (data.inversed) i-- else i++
                t += timeline[i].timeInterval / duration.toFloat()
            }
            data.setProperty(timeline[i].value)
        } else {
            // animation finished
            if (data.resetOnFinish)
                data.setProperty(timeline[0].value)
            dispose(data)
            data.callback()
        }
    }

    override fun componentType() = Animation
    companion object : SystemComponentSubType<Animation, TimelineIntAnimation>(Animation, TimelineIntAnimation::class) {
        override fun createEmpty() = TimelineIntAnimation()
    }
}