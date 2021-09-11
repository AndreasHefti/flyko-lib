package com.inari.firefly.graphics.view

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.geom.Position
import com.inari.util.geom.PositionF
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField


class Layer private constructor() : SystemComponent(Layer::class.simpleName!!) {

    @JvmField internal var viewRef = -1

    val view = ComponentRefResolver(View) { index->
        viewRef = setIfNotInitialized(index, "view")
    }
    @JvmField var zPosition: Int = 0

    override fun toString(): String = "Layer(view=$viewRef)"

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Layer>(Layer::class) {
        override fun createEmpty() = Layer()
    }
}