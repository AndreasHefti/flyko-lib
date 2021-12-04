package com.inari.firefly.graphics.view

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.FrameBufferData
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class FrameBuffer private constructor() : SystemComponent(FrameBuffer::class.simpleName!!) {

    @JvmField internal var bufferId: Int = -1
    @JvmField internal val data = FrameBufferData()

    var bounds: Vector4i
        get() = data.bounds
        set(value) { data.bounds(value) }
    @JvmField var clear: Boolean = true
    var clearColor: Vector4f
        get() = data.clearColor
        set(value) { data.clearColor(value) }
    var tintColor: Vector4f
        get() = data.tintColor
        set(value) { data.tintColor(value) }
    var blendMode: BlendMode
        get() = data.blendMode
        set(value) { data.blendMode = value }
    @JvmField val shader = AssetInstanceRefResolver(
        { instanceId -> data.shaderRef = instanceId },
        { data.shaderRef })
    var zoom: Float
        get() = data.zoom
        set(value) { data.zoom = value }
    var fboScale: Float
        get() = data.fboScale
        set(value) { data.fboScale = value }

    internal fun activate() {
        if (bufferId >= 0)
            return

        bufferId = FFContext.graphics.createFrameBuffer(data)
    }

    internal fun deactivate() {
        if (bufferId < 0)
            return

        FFContext.graphics.disposeFrameBuffer(bufferId)
        bufferId = -1
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<FrameBuffer>(FrameBuffer::class) {
        override fun createEmpty() = FrameBuffer()
    }
}