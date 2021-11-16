package com.inari.firefly.core.api

import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class SpriteData(
    @JvmField var textureId: Int = -1,
    @JvmField val region: Vector4i = Vector4i(),
    @JvmField var isHorizontalFlip: Boolean = false,
    @JvmField var isVerticalFlip: Boolean = false
) {

    fun reset() {
        textureId = -1
        region.x = 0
        region.y = 0
        region.width = 0
        region.height = 0
        isHorizontalFlip = false
        isVerticalFlip = false
    }
}
