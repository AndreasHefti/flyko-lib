package com.inari.firefly.graphics.view

import com.inari.firefly.BlendMode
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.control.ControlledSystemComponent
import com.inari.firefly.core.api.ViewData
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
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


    @JvmField var zPosition: Int = 0
    var bounds: Vector4i
        get() = data.bounds
        set(value) { data.bounds(value) }
    var worldPosition: Vector2f
        get() = data.worldPosition
        set(value) { data.worldPosition(value) }
    var clearColor: Vector4f
        get() = data.clearColor
        set(value) { data.clearColor(value) }
    var tintColor: Vector4f
        get() = data.tintColor
        set(value) { data.tintColor(value) }
    var blendMode: BlendMode
        get() = data.blendMode
        set(value) { data.blendMode = value }
    val shader = AssetInstanceRefResolver(
        { instanceId -> data.shaderRef = instanceId },
        { data.shaderRef })
    var zoom: Float
        get() = data.zoom
        set(value) { data.zoom = value }
    var fboScale: Float
        get() = data.fboScale
        set(value) { data.fboScale = value }

    private val layers = mutableListOf<Layer.() -> Unit>()
    fun withLayer(configure: Layer.() -> Unit) =
        layers.add(configure)

    override fun init() {
        super.init()
        val it = layers.iterator()
        while (it.hasNext())
            Layer.build {
                view(this@View.index)
                also(it.next())
            }
        layers.clear()
    }

    override fun dispose() {
        layers.clear()
        disposeController()
        super.dispose()
    }

    override fun toString(): String {
        return "View(baseView=$baseView, " +
            "bounds=${data.bounds}, " +
            "worldPosition=${data.worldPosition}, " +
            "clearColor=${data.clearColor}, " +
            "tintColor=${data.tintColor}, " +
            "blendMode=${data.blendMode}, " +
            "shaderRef=${data.shaderRef}, " +
            "zoom=${data.zoom}, " +
            "fboScale=${data.fboScale})"
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<View>(View::class) {
        override fun createEmpty() = View()
    }
}