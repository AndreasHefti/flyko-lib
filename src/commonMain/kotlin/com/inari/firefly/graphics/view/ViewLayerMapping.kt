package com.inari.firefly.graphics.view

import com.inari.firefly.core.component.CompId
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO

interface ViewLayerAware {
    val viewIndex: Int
    val layerIndex: Int
}

class SimpleViewLayerAware(
    override var viewIndex: Int = -1,
    override var layerIndex: Int = -1
) : ViewLayerAware

class ViewLayerMapping<C : ViewLayerAware> constructor(
    val createFunction: () -> DynArray<C>
) {

    private val mapping: DynArray<DynArray<C>> = DynArray.of()

    fun contains(view: Int, layer: Int): Boolean = view in mapping && layer in mapping[view]!!
    operator fun contains(view: Int): Boolean = view in mapping
    operator fun contains(view: CompId): Boolean = view.instanceId in mapping
    operator fun contains(view: View): Boolean = view.index in mapping
    operator fun contains(viewLayer: ViewLayerAware): Boolean = contains(viewLayer.viewIndex, viewLayer.layerIndex)

    operator fun get(view: Int): DynArrayRO<C> = internalGet(view)!!
    operator fun get(view: CompId): DynArrayRO<C> = get(view.instanceId)
    operator fun get(view: View): DynArrayRO<C> = get(view.index)
    operator fun get(view: Int, layer: Int): C? = this[view][layer]
    operator fun get(viewLayer: ViewLayerAware): C? = this[viewLayer.viewIndex][viewLayer.layerIndex]
    operator fun get(view: CompId, layer: CompId): C? = this[view][layer.instanceId]
    operator fun get(view: View, layer: Layer): C? = this[view][layer.index]

    fun add(c: C) = internalGet(c.viewIndex).set(c.layerIndex, c)
    fun delete(c: C) = internalGet(c.viewIndex).remove(c.layerIndex)
    fun deleteAll(view: Int, layer: Int) = mapping[view]?.remove(layer)
    fun deleteAll(view: Int) = mapping.remove(view)
    fun clear() = mapping.clear()

    private fun internalGet(view: Int): DynArray<C> {
        if (view !in mapping)
            mapping[view] = createFunction()
        return mapping[view]!!
    }

    companion object {
        inline fun <reified C: ViewLayerAware> of(): ViewLayerMapping<C> =
                ViewLayerMapping { DynArray.of<C>() }
    }
}

class ViewLayerListMapping <C : ViewLayerAware> constructor(
    val createFunction: () -> DynArray<C>
) {

    private val mapping: DynArray<DynArray<DynArray<C>>> = DynArray.of()

    fun contains(view: Int, layer: Int): Boolean = view in mapping && layer in mapping[view]!!
    operator fun contains(view: Int): Boolean = view in mapping
    operator fun contains(view: CompId): Boolean = view.instanceId in mapping
    operator fun contains(view: View): Boolean = view.index in mapping
    operator fun contains(viewLayer: ViewLayerAware): Boolean = contains(viewLayer.viewIndex, viewLayer.layerIndex)

    operator fun get(view: Int, layer: Int): DynArrayRO<C>? = internalGet(view, layer)
    operator fun get(viewLayer: ViewLayerAware): DynArrayRO<C>? = internalGet(viewLayer.viewIndex, viewLayer.layerIndex)
    operator fun get(view: CompId, layer: CompId):DynArrayRO<C>? = internalGet(view.instanceId, layer.instanceId)
    operator fun get(view: View, layer: Layer): DynArrayRO<C>? = internalGet(view.index, layer.index)

    fun add(c: C) = internalGet(c.viewIndex, c.layerIndex).add(c)
    fun delete(c: C) = internalGet(c.viewIndex, c.layerIndex).remove(c)
    fun deleteAll(view: Int, layer: Int) = mapping[view]?.remove(layer)
    fun deleteAll(view: Int) = mapping.remove(view)
    fun clear() = mapping.clear()

    private fun internalGet(view: Int, layer: Int): DynArray<C> {
        if (view !in mapping)
            mapping[view] = DynArray.of(1, 1)

        val perView = mapping[view]!!
        if (layer !in perView)
            perView[layer] = createFunction()

        return perView[layer]!!
    }

    companion object {
        inline fun <reified C: ViewLayerAware> of(): ViewLayerListMapping<C> =
            ViewLayerListMapping { DynArray.of(1, 1) }
    }
}