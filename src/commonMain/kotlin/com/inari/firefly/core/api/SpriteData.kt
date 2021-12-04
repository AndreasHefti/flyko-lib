package com.inari.firefly.core.api

import com.inari.firefly.ZERO_INT
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class SpriteData(
    @JvmField internal var textureId: Int = -1,
    @JvmField internal val region: Vector4i = Vector4i(),
    @JvmField internal var isHorizontalFlip: Boolean = false,
    @JvmField internal var isVerticalFlip: Boolean = false
) {

    fun reset() {
        textureId = -1
        region(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
        isHorizontalFlip = false
        isVerticalFlip = false
    }
}
