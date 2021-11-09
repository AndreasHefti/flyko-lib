package com.inari.firefly.graphics.view

import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.Position
import com.inari.util.geom.PositionF
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField


class Layer private constructor() : SystemComponent(Layer::class.simpleName!!) {

    @JvmField internal var viewRef = -1
    @JvmField internal var shaderRef = -1

    val view = ComponentRefResolver(View) { index->
        viewRef = setIfNotInitialized(index, "view")
    }
    @JvmField var zPosition: Int = 0
    val shader = AssetInstanceRefResolver(
        { instanceId -> shaderRef = instanceId },
        { shaderRef })

    override fun componentType() = Companion
    override fun toString(): String {
        return "Layer(viewRef=$viewRef, shaderRef=$shaderRef, zPosition=$zPosition)"
    }

    companion object : SystemComponentSingleType<Layer>(Layer::class) {
        override fun createEmpty() = Layer()
    }
}