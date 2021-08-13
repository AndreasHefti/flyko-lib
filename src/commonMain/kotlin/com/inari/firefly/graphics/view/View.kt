package com.inari.firefly.graphics.view

import com.inari.firefly.BlendMode
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.control.ControlledSystemComponent
import com.inari.firefly.control.ControllerSystem
import com.inari.firefly.core.api.ViewData
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.PositionF
import com.inari.util.geom.Rectangle
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class View private constructor (
    @JvmField internal var baseView: Boolean = false
) : SystemComponent(View::class.simpleName!!), ControlledSystemComponent {

    @JvmField internal val data = object : ViewData() {
        override val index: Int
            get() = super@View.index
        override val isBase: Boolean
            get() = baseView
    }

    var bounds: Rectangle
        get() = data.bounds
        set(value) { data.bounds(value) }
    var worldPosition: PositionF
        get() = data.worldPosition
        set(value) { data.worldPosition(value) }
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
        { index -> data.shaderId = index },
        { data.shaderId })
    var zoom: Float
        get() = data.zoom
        set(value) { data.zoom = value }
    var fboScale: Float
        get() = data.fboScale
        set(value) { data.fboScale = value }

    override fun init() {
        super.init()
    }

    override fun dispose() {
        ControllerSystem.unregister(componentId, true)
        super.dispose()
    }

    override fun toString(): String {
        return "View(baseView=$baseView, " +
            "bounds=${data.bounds}, " +
            "worldPosition=${data.worldPosition}, " +
            "clearColor=${data.clearColor}, " +
            "tintColor=${data.tintColor}, " +
            "blendMode=${data.blendMode}, " +
            "shaderId=${data.shaderId}, " +
            "zoom=${data.zoom}, " +
            "fboScale=${data.fboScale})"
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<View>(View::class) {
        override fun createEmpty() = View()
    }
}