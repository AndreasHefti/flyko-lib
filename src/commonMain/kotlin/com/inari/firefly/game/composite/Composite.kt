package com.inari.firefly.game.composite

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

open class Composite protected constructor(subType: ComponentType<out Composite>) : ComponentNode(subType), ViewLayerAware {

    constructor() : this(Composite)

    override val viewIndex: Int
        get() = viewRef.targetKey.instanceIndex
    override val layerIndex: Int
        get() = layerRef.targetKey.instanceIndex

    @JvmField var viewRef = CReference(View)
    @JvmField var layerRef = CReference(Layer)
    @JvmField val position = Vector2f()

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