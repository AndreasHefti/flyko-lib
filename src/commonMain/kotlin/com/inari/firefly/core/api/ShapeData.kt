package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Vector4f
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
    @JvmField internal var type: ShapeType = ShapeType.POINT,
    @JvmField internal var vertices: FloatArray = floatArrayOf(),
    @JvmField internal var segments: Int = -1,
    @JvmField internal var color1: Vector4f = Vector4f(1f, 1f, 1f, 1f),
    @JvmField internal var color2: Vector4f? = null,
    @JvmField internal var color3: Vector4f? = null,
    @JvmField internal var color4: Vector4f? = null,
    @JvmField internal var blend: BlendMode = BlendMode.NONE,
    @JvmField internal var fill: Boolean = false
) {

    fun reset() {
        type = ShapeType.POINT
        vertices = floatArrayOf()
        segments = -1
        color1 = Vector4f(1f, 1f, 1f, 1f)
        color2 = null
        color3 = null
        color4 = null
        blend = BlendMode.NONE
        fill = false
    }
}


