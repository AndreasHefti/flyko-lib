package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

enum class ShapeType {
    POINT,
    LINE,
    POLY_LINE,
    POLYGON,
    RECTANGLE,
    CIRCLE,
    ARC,
    CURVE,
    TRIANGLE
}

class ShapeData(
    @JvmField var type: ShapeType = ShapeType.POINT,
    @JvmField var vertices: FloatArray = floatArrayOf(),
    @JvmField var segments: Int = -1,
    @JvmField var color1: MutableColor = MutableColor(1f, 1f, 1f, 1f),
    @JvmField var color2: MutableColor? = null,
    @JvmField var color3: MutableColor? = null,
    @JvmField var color4: MutableColor? = null,
    @JvmField var blend: BlendMode = BlendMode.NONE,
    @JvmField var fill: Boolean = false,
    @JvmField var effectInstanceRef: Int = -1
) {

    fun reset() {
        type = ShapeType.POINT
        vertices = floatArrayOf()
        segments = -1
        color1 = MutableColor(1f, 1f, 1f, 1f)
        color2 = null
        color3 = null
        color4 = null
        blend = BlendMode.NONE
        fill = false
        effectInstanceRef = -1
    }
}


