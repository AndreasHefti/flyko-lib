package com.inari.firefly.physics.animation

import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite

interface TimelineFrame {

    val timeInterval: Long

    interface IntFrame : TimelineFrame {
        val value: Int
    }
    interface FloatFrame : TimelineFrame {
        val value: Float
    }
    interface ValueFrame<out T> : TimelineFrame {
        val value: T
    }

    @ComponentDSL
    class SpriteFrame : IntFrame {

        var interval: Long = 0
        var sprite: ProtoSprite = ProtoSprite()

        val protoSprite: (ProtoSprite.() -> Unit) -> Unit = { configure ->
            val sprite = ProtoSprite()
            sprite.also(configure)
            this.sprite = sprite
        }

        override val timeInterval: Long
            get() { return interval }

        override val value: Int
            get() = sprite.instanceId

        companion object {
            val of: (SpriteFrame.() -> Unit) -> SpriteFrame = { configure ->
                val instance = SpriteFrame()
                instance.also(configure)
                instance
            }
        }
    }
}