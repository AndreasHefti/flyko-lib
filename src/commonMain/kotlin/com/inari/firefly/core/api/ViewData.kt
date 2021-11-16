package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

abstract class ViewData constructor(
    @JvmField var bounds: Vector4i = Vector4i(),
    @JvmField var worldPosition: Vector2f = Vector2f(),
    @JvmField var clearColor: Vector4f = Vector4f( 0f, 0f, 0f, 1f ),
    @JvmField var tintColor: Vector4f = Vector4f( 1f, 1f, 1f, 1f ),
    @JvmField var blendMode: BlendMode = BlendMode.NONE,
    @JvmField var shaderRef: Int = -1,
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
                "shaderRef=$shaderRef, " +
                "zoom=$zoom, " +
                "fboScale=$fboScale, " +
                "index=$index, " +
                "isBase=$isBase)"
    }
}