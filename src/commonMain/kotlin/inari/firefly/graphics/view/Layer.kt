package com.inari.firefly.graphics.view

import com.inari.firefly.core.Asset
import com.inari.firefly.core.CReference
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentSystem
import kotlin.jvm.JvmField

class Layer private constructor(): Component(Layer) {

    @JvmField var view = CReference(View)
    @JvmField var zPosition = 0
    val shader = CReference(Shader)
    var shaderIndex = -1
        internal set

    override fun notifyParent(comp: Component) {
        if (comp.componentType.aspectIndex == View.aspectIndex)
            shader(comp)
    }

    override fun activate() {
        shaderIndex = Asset.resolveAssetIndex(shader.targetKey)
    }

    override fun deactivate() {
        shaderIndex = -1
    }

    override val componentType = Companion
    companion object : ComponentSystem<Layer>("Layer") {
        override fun allocateArray(size: Int): Array<Layer?> = arrayOfNulls(size)
        override fun create() = Layer()
    }
}