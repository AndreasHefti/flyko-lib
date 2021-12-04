package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class FrameBufferData(
    @JvmField internal var bounds: Vector4i = Vector4i(),
    @JvmField internal var clearColor: Vector4f = Vector4f( 0f, 0f, 0f, 1f ),
    @JvmField internal var tintColor: Vector4f = Vector4f( 1f, 1f, 1f, 1f ),
    @JvmField internal var blendMode: BlendMode = BlendMode.NONE,
    @JvmField internal var shaderRef: Int = -1,
    @JvmField internal var zoom: Float = 1.0f,
    @JvmField internal var fboScale: Float = 1.0f
)