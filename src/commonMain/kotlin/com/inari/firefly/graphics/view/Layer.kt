package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class Layer private constructor(): Composite(Layer), Controlled {

    //override val controllerReferences = ControllerReferences(Layer)

    @JvmField val viewRef = CReference(View)
    @JvmField var zPosition = 0
    /** The position (offset) relative to the referenced view of this layer */
    @JvmField val position = Vector2f()
    val shaderRef = CReference(Shader)
    var shaderIndex: Int = -1
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
        shaderIndex = -1
    }

    companion object : ComponentSystem<Layer>("Layer") {
        override fun allocateArray(size: Int): Array<Layer?> = arrayOfNulls(size)
        override fun create() = Layer()
    }
}