package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import kotlin.jvm.JvmField

class Layer private constructor(): ComponentNode(Layer) {

    @JvmField var view = CReference(View)
    @JvmField var zPosition = 0
    val shader = CReference(Shader)
    var shaderIndex: Int = -1
        internal set

    override fun setParentComponent(key: ComponentKey) {
        super.setParentComponent(key)
        if (key.type.aspectIndex == View.aspectIndex)
            view(key)
        else
            view.reset()
    }

    override fun activate() {
        shaderIndex = Asset.resolveAssetIndex(shader.targetKey)
    }

    override fun deactivate() {
        shaderIndex = -1
    }

    companion object : ComponentSystem<Layer>("Layer") {
        override fun allocateArray(size: Int): Array<Layer?> = arrayOfNulls(size)
        override fun create() = Layer()
    }
}