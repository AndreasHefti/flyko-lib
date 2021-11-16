package com.inari.firefly.game.tile

import com.inari.firefly.NO_NAME
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite
import com.inari.firefly.physics.animation.TimelineFrame
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

@ComponentDSL
class TileAnimation internal constructor() {

    @JvmField internal val frames:  DynArray<TimelineFrame.SpriteFrame> = DynArray.of(5, 5)
    @JvmField internal val sprites: MutableMap<String, ProtoSprite> = mutableMapOf()

    val withFrame: (TimelineFrame.SpriteFrame.() -> Unit) -> Unit = { configure ->
        val frame = TimelineFrame.SpriteFrame()
        frame.also(configure)

        if (frame.sprite.name == NO_NAME)
                throw IllegalArgumentException("Missing name")

        frames.add(frame)
        sprites[frame.sprite.name] = frame.sprite
    }
}