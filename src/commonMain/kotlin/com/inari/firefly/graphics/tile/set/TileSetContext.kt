package com.inari.firefly.graphics.tile.set

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray
import com.inari.util.indexed.Indexed

object TileSetContext {

    private val activeTileEntityRefs: DynArray<DynIntArray> = DynArray.of()

    operator fun get(tileSetName: String): TileSet =
        FFContext[TileSet, tileSetName]

    operator fun get(tileSetId: Int): TileSet =
        FFContext[TileSet, tileSetId]

    internal fun addActiveTileEntityId(entityRef: Int, layerRef: Int) {
        if (layerRef !in activeTileEntityRefs)
            activeTileEntityRefs.add(DynIntArray(100, -1, 100))
        val activeIdsOfLayer = activeTileEntityRefs[layerRef]!!
        activeIdsOfLayer.add(entityRef)
    }

    internal fun removeActiveTileEntityId(entityRef: Int, layerRef: Int) {
        if (layerRef !in activeTileEntityRefs)
            return
        val activeIdsOfLayer = activeTileEntityRefs[layerRef]!!
        activeIdsOfLayer.remove(entityRef)
    }

    internal fun updateActiveTileEntityRefs(layerRef: Int) {
        if (layerRef !in activeTileEntityRefs)
            return
        activeTileEntityRefs[layerRef]!!.trim_head_tail()
    }

    fun getTileEntityRef(code: Int, layer: CompId): Int =
        getTileEntityRef(code, layer.index)

    fun getTileEntityRef(code: Int, layer: Indexed): Int =
        getTileEntityRef(code, layer.index)

    fun getTileEntityRef(code: Int, layerRef: Int): Int =
        activeTileEntityRefs[layerRef]?.get(code) ?: -1
}