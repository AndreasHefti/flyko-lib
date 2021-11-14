package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Rectangle
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class BackBufferData(
    @JvmField var bounds: Rectangle = Rectangle(),
    @JvmField var clearColor: Vector4f = Vector4f( 0f, 0f, 0f, 1f ),
    @JvmField var tintColor: Vector4f = Vector4f( 1f, 1f, 1f, 1f ),
    @JvmField var blendMode: BlendMode = BlendMode.NONE,
    @JvmField var viewportRef: Int = -1,
    @JvmField var shaderRef: Int = -1,
    @JvmField var zoom: Float = 1.0f,
    @JvmField var fboScale: Float = 1.0f
)