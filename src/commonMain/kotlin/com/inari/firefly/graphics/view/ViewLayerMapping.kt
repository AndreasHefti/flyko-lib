package com.inari.firefly.graphics.view

import com.inari.firefly.core.ComponentKey
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetRO
import com.inari.util.collection.DynArray

interface ViewLayerAware {
    val viewIndex: Int
    val layerIndex: Int
}

class SimpleViewLayerAware(
    override var viewIndex: Int = -1,
    override var layerIndex: Int = -1
) : ViewLayerAware

class ViewLayerMapping {

    private val mapping: DynArray<DynArray<BitSet>> = DynArray.of()

    operator fun get(view: Int, layer: Int): BitSetRO = internalGet(view, layer)
    operator fun get(viewLayer: ViewLayerAware): BitSetRO = internalGet(viewLayer.viewIndex, viewLayer.layerIndex)
    operator fun get(view: ComponentKey, layer: ComponentKey): BitSetRO = internalGet(view.instanceId, layer.instanceId)
    operator fun get(view: View, layer: Layer): BitSetRO = internalGet(view.index, layer.index)

    fun add(viewLayer: ViewLayerAware, index: Int) =
        internalGet(viewLayer.viewIndex, viewLayer.layerIndex).set(index, true)
    fun add(viewIndex: Int, layerIndex: Int, index: Int) =
        internalGet(viewIndex, layerIndex).set(index, true)
    fun delete(viewLayer: ViewLayerAware, index: Int) =
        internalGet(viewLayer.viewIndex, viewLayer.layerIndex).set(index, false)
    fun delete(viewIndex: Int, layerIndex: Int, index: Int) =
        internalGet(viewIndex, layerIndex).set(index, false)
    fun deleteAll(view: Int, layer: Int) = mapping[view]?.remove(layer)
    fun deleteAll(view: Int) = mapping.remove(view)
    fun clear() = mapping.clear()

    private fun internalGet(view: Int, layer: Int): BitSet {
        if (view !in mapping)
            mapping[view] = DynArray.of(1, 1)

        val perView = mapping[view]!!
        if (layer !in perView)
            perView[layer] = BitSet()

        return perView[layer]!!
    }
}