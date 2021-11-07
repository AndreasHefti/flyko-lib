package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Rectangle
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class BackBufferData(
    @JvmField var bounds: Rectangle = Rectangle(),
    @JvmField var clearColor: MutableColor = MutableColor( 0f, 0f, 0f, 1f ),
    @JvmField var tintColor: MutableColor = MutableColor( 1f, 1f, 1f, 1f ),
    @JvmField var blendMode: BlendMode = BlendMode.NONE,
    @JvmField var viewportRef: Int = -1,
    @JvmField var shaderRef: Int = -1,
    @JvmField var zoom: Float = 1.0f,
    @JvmField var fboScale: Float = 1.0f
) {
}