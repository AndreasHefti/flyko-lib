package com.inari.firefly.graphics.tile.set

import com.inari.firefly.NO_NAME
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite
import com.inari.firefly.physics.animation.timeline.Frame
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

@ComponentDSL
class TileAnimation internal constructor() {

    @JvmField internal val frames:  DynArray<Frame.SpriteFrame> = DynArray.of(5, 5)
    @JvmField internal val sprites: MutableMap<String, ProtoSprite> = mutableMapOf()

    val frame: (Frame.SpriteFrame.() -> Unit) -> Unit = { configure ->
        val frame = Frame.SpriteFrame()
        frame.also(configure)

        if (frame.sprite.name == NO_NAME)
                throw IllegalArgumentException("Missing name")

        frames.add(frame)
        sprites[frame.sprite.name] = frame.sprite
    }

    companion object {

        val of: (TileAnimation.() -> Unit) -> TileAnimation = { configure ->
            val comp = TileAnimation()
            comp.also(configure)
            comp
        }

    }
}