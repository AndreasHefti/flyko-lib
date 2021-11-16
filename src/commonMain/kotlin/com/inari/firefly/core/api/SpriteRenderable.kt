
package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField


class SpriteRenderable(
    @JvmField var spriteId: Int = -1,
    @JvmField var tintColor: Vector4f = Vector4f(1f, 1f, 1f, 1f),
    @JvmField var blendMode: BlendMode = BlendMode.NONE
) {

    fun reset() {
        spriteId = -1
        tintColor = Vector4f(1f, 1f, 1f, 1f)
        blendMode = BlendMode.NONE
    }

    override fun toString(): String =
        "SpriteRenderable(spriteId=$spriteId, tintColor=$tintColor, blendMode=$blendMode)"

}
