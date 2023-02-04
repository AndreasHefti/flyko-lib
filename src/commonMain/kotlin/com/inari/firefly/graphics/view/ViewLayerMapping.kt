package com.inari.firefly.graphics.view

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetRO
import com.inari.util.collection.DynArray

interface ViewLayerAware {
    val viewIndex: ComponentIndex
    val layerIndex: ComponentIndex
}

class SimpleViewLayerAware(
    override var viewIndex: ComponentIndex = NULL_COMPONENT_INDEX,
    override var layerIndex: ComponentIndex = NULL_COMPONENT_INDEX
) : ViewLayerAware

class ViewLayerMapping {

    private val mapping: DynArray<DynArray<BitSet>> = DynArray.of()

    operator fun get(view: ComponentIndex, layer: ComponentIndex): BitSetRO = internalGet(view, layer)
    operator fun get(viewLayer: ViewLayerAware): BitSetRO = internalGet(viewLayer.viewIndex, viewLayer.layerIndex)
    operator fun get(view: ComponentKey, layer: ComponentKey): BitSetRO = internalGet(view.componentIndex, layer.componentIndex)
    operator fun get(view: View, layer: Layer): BitSetRO = internalGet(view.index, layer.index)

    fun add(viewLayer: ViewLayerAware, index: ComponentIndex) =
        internalGet(viewLayer.viewIndex, viewLayer.layerIndex).set(index, true)
    fun add(viewIndex: ComponentIndex, layerIndex: ComponentIndex, index: ComponentIndex) =
        internalGet(viewIndex, layerIndex).set(index, true)
    fun delete(viewLayer: ViewLayerAware, index: ComponentIndex) =
        internalGet(viewLayer.viewIndex, viewLayer.layerIndex).set(index, false)
    fun delete(viewIndex: ComponentIndex, layerIndex: ComponentIndex, index: ComponentIndex) =
        internalGet(viewIndex, layerIndex).set(index, false)
    fun deleteAll(view: ComponentIndex, layer: ComponentIndex) = mapping[view]?.remove(layer)
    fun deleteAll(view: ComponentIndex) = mapping.remove(view)
    fun clear() = mapping.clear()

    private fun internalGet(view: ComponentIndex, layer: ComponentIndex): BitSet {
        if (view !in mapping)
            mapping[view] = DynArray.of(1, 1)

        val l = if (layer >= 0) layer else 0
        val perView = mapping[view]!!
        if (layer !in perView)
            perView[l] = BitSet()

        return perView[l]!!
    }
}