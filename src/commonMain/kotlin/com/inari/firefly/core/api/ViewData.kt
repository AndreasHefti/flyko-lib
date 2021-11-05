package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.PositionF
import com.inari.util.geom.Rectangle
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

abstract class ViewData constructor(
    @JvmField var bounds: Rectangle = Rectangle(),
    @JvmField var worldPosition: PositionF = PositionF(),
    @JvmField var clearColor: MutableColor = MutableColor( 0f, 0f, 0f, 1f ),
    @JvmField var tintColor: MutableColor = MutableColor( 1f, 1f, 1f, 1f ),
    @JvmField var blendMode: BlendMode = BlendMode.NONE,
    @JvmField var effectInstanceRef: Int = -1,
    @JvmField var zoom: Float = 1.0f,
    @JvmField var fboScale: Float = 1.0f
) {
    abstract val index: Int
    abstract val isBase: Boolean

    override fun toString(): String {
        return "ViewData(" +
                "bounds=$bounds, " +
                "worldPosition=$worldPosition, " +
                "clearColor=$clearColor, " +
                "tintColor=$tintColor, " +
                "blendMode=$blendMode, " +
                "effectInstanceRef=$effectInstanceRef, " +
                "zoom=$zoom, " +
                "fboScale=$fboScale, " +
                "index=$index, " +
                "isBase=$isBase)"
    }
}