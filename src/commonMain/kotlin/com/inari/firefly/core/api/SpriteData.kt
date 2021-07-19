package com.inari.firefly.core.api

import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField

class SpriteData(
    @JvmField var textureId: Int = -1,
    @JvmField val region: Rectangle = Rectangle(),
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
