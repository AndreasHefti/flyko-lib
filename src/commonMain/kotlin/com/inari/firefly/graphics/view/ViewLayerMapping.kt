package com.inari.firefly.graphics.view

import com.inari.firefly.core.component.CompId
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO

class ViewLayerMapping<C : ViewLayerAware> constructor(
    val createFunction: () -> DynArray<C>
) {

    private val mapping: DynArray<DynArray<C>> = DynArray.of()

    operator fun contains(view: Int): Boolean =
        view in mapping

    operator fun contains(view: CompId): Boolean =
        view.instanceId in mapping

    operator fun contains(view: View): Boolean =
        view.index in mapping

    operator fun contains(viewLayer: ViewLayerAware): Boolean =
        viewLayer.viewIndex in mapping &&
        viewLayer.layerIndex in mapping[viewLayer.viewIndex]!!

    fun contains(view: Int, layer: Int): Boolean =
        view in mapping &&
            layer in mapping[view]!!

    operator fun get(view: Int): DynArrayRO<C> {
        if (view !in mapping)
            mapping[view] = createFunction()
        return mapping[view]!!
    }

    operator fun get(view: CompId): DynArrayRO<C> =
        get(view.instanceId)

    operator fun get(view: View): DynArrayRO<C> =
        get(view.index)

    operator fun get(view: Int, layer: Int): C? =
        this[view][layer]

    operator fun get(viewLayer: ViewLayerAware): C? =
        this[viewLayer.viewIndex][viewLayer.layerIndex]

    operator fun get(view: CompId, layer: CompId): C? =
        this[view][layer.instanceId]

    operator fun get(view: View, layer: Layer): C? =
        this[view][layer.index]

    fun add(c: C) {
        internalGet(c.viewIndex)?.set(c.layerIndex, c)
    }

    fun delete(c: C) {
        internalGet(c.viewIndex)?.remove(c.layerIndex)
    }

    private fun internalGet(view: Int): DynArray<C>? {
        if (view !in mapping)
            mapping[view] = createFunction()
        return mapping[view]
    }

    companion object {
        inline fun <reified C: ViewLayerAware> of(): ViewLayerMapping<C> =
                ViewLayerMapping { DynArray.of<C>() }
    }

}