
package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField


class SpriteRenderable(
    @JvmField var spriteId: Int = -1,
    @JvmField var tintColor: MutableColor = MutableColor(1f, 1f, 1f, 1f),
    @JvmField var blendMode: BlendMode = BlendMode.NONE,
    @JvmField var shaderRef: Int = -1
) {

    fun reset() {
        spriteId = -1
        tintColor = MutableColor(1f, 1f, 1f, 1f)
        blendMode = BlendMode.NONE
        shaderRef = -1
    }

    override fun toString(): String =
        "SpriteRenderable(spriteId=$spriteId, tintColor=$tintColor, blendMode=$blendMode, shaderRef=$shaderRef)"

}
