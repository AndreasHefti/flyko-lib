package com.inari.firefly.graphics.effect

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.BackBufferData
import com.inari.firefly.core.api.FFGraphics
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Rectangle
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class BackBufferAsset private constructor() : Asset() {

    @JvmField internal var backBufferId: Int = -1
    @JvmField internal val data = BackBufferData()

    var bounds: Rectangle
        get() = data.bounds
        set(value) { data.bounds(value) }
    var clearColor: MutableColor
        get() = data.clearColor
        set(value) { data.clearColor(value) }
    var tintColor: MutableColor
        get() = data.tintColor
        set(value) { data.tintColor(value) }
    var blendMode: BlendMode
        get() = data.blendMode
        set(value) { data.blendMode = value }
    val shader = AssetInstanceRefResolver(
        { instanceId -> data.shaderRef = instanceId },
        { data.shaderRef })
    val view = ComponentRefResolver(View) { index-> data.viewportRef = index }
    var zoom: Float
        get() = data.zoom
        set(value) { data.zoom = value }
    var fboScale: Float
        get() = data.fboScale
        set(value) { data.fboScale = value }

    override fun instanceId(index: Int): Int = backBufferId

    override fun load() {
        if (backBufferId >= 0)
            return

        backBufferId = FFContext.graphics.createBackBuffer(data)
    }

    override fun unload() {
        if (backBufferId < 0)
            return

        FFContext.graphics.disposeBackBuffer(backBufferId)
        backBufferId = -1
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, BackBufferAsset>(Asset, BackBufferAsset::class) {
        override fun createEmpty() = BackBufferAsset()
    }
}