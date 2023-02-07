package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class Layer private constructor(): Component(Layer), Controlled {

    @JvmField val viewRef = CReference(View)
    @JvmField var zPosition = 0
    /** The position (offset) relative to the referenced view of this layer */
    @JvmField val position = Vector2f()
    val shaderRef = CReference(Shader)
    var shaderIndex: ComponentIndex = NULL_COMPONENT_INDEX
        internal set

    fun withShader(configure: (Shader.() -> Unit)): ComponentKey {
        shaderRef(Shader.build(configure))
        return shaderRef.targetKey
    }

    override fun activate() {
        if (shaderRef.targetKey !== NO_COMPONENT_KEY)
            shaderIndex = Asset.resolveAssetIndex(shaderRef.targetKey)
    }

    override fun deactivate() {
        shaderIndex = NULL_COMPONENT_INDEX
    }

    companion object : ComponentSystem<Layer>("Layer") {
        override fun allocateArray(size: Int): Array<Layer?> = arrayOfNulls(size)
        override fun create() = Layer()
    }
}