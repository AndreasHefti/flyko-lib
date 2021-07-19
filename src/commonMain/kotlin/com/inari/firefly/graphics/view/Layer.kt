package com.inari.firefly.graphics.view

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import kotlin.jvm.JvmField


class Layer private constructor() : SystemComponent(Layer::class.simpleName!!) {

    @JvmField internal var viewRef = -1

    val view = ComponentRefResolver(View) { index->
        viewRef = setIfNotInitialized(index, "view")
    }

    override fun toString(): String = "Layer(view=$viewRef)"

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Layer>(Layer::class) {
        override fun createEmpty() = Layer()
    }
}