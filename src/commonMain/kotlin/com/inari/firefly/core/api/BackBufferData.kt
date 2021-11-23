package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class BackBufferData(
    @JvmField inline var bounds: Vector4i = Vector4i(),
    @JvmField inline var clearColor: Vector4f = Vector4f( 0f, 0f, 0f, 1f ),
    @JvmField inline var tintColor: Vector4f = Vector4f( 1f, 1f, 1f, 1f ),
    @JvmField inline var blendMode: BlendMode = BlendMode.NONE,
    @JvmField inline var viewportRef: Int = -1,
    @JvmField inline var shaderRef: Int = -1,
    @JvmField inline var zoom: Float = 1.0f,
    @JvmField inline var fboScale: Float = 1.0f
)