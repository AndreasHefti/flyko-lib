package com.inari.firefly.game.composite

import com.inari.firefly.core.CReference
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

abstract class Composite protected constructor() : Component(Composite) {

    @JvmField internal val attributes = mutableMapOf<String, String>()

    fun setAttribute(name: String, value: String) { attributes[name] = value }
    fun getAttribute(name: String): String? = attributes[name]
    fun getAttributeFloat(name: String): Float? = attributes[name]?.toFloat()

    companion object : ComponentSystem<Composite>("Composite") {
        override fun allocateArray(size: Int): Array<Composite?> = arrayOfNulls(size)
        override fun create(): Composite =
            throw UnsupportedOperationException("Composite is abstract use a concrete implementation instead")
    }
}

abstract class WordViewComposite protected constructor() : Composite() {

    val viewIndex: Int
        get() = view.targetKey.instanceIndex
    val layerIndex: Int
        get() = layer.targetKey.instanceIndex
    @JvmField var view = CReference(View)
    @JvmField var layer = CReference(Layer)
    @JvmField val position = Vector2f()

}