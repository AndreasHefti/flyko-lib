package com.inari.firefly.graphics.tile

import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.Vector4i

object SimpleTileGridRenderer : EntityRenderer("SimpleTileGridRenderer") {

    override fun acceptEntity(index: EntityIndex) = false

    override fun sort(entities: DynIntArray) {
        // not sorting
    }

    val graphics = Engine.graphics
    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {

        val tileGrids = TileGrid[viewIndex, layerIndex]
        if (tileGrids.isEmpty) return

        var tileGridIndex = tileGrids.nextSetBit(0)
        while (tileGridIndex >= 0) {
            val tileGrid = TileGrid[tileGridIndex]
            if (!tileGrid.active) continue
            if (tileGrid.viewIndex != viewIndex || tileGrid.layerIndex != layerIndex) continue

            val iterator = tileGrid.tileGridIterator(clip)
            Engine.graphics.applyViewportOffset(-tileGrid.position.x, -tileGrid.position.y)
            while (iterator.hasNext()) {
                graphics.renderSprite(
                    ETile[iterator.next()].renderData,
                    iterator.worldPosition.x,
                    iterator.worldPosition.y
                )
            }
            Engine.graphics.applyViewportOffset(tileGrid.position.x, tileGrid.position.y)
            tileGridIndex = tileGrids.nextSetBit(tileGridIndex + 1)
        }
    }

    override fun render(entities: DynIntArray) =
        throw UnsupportedOperationException()
}

object FullTileGridRenderer : EntityRenderer("FullTileGridRenderer") {

    override fun acceptEntity(index: EntityIndex) = false

    override fun sort(entities: DynIntArray) {
        // not sorting
    }

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val graphics = Engine.graphics
        val tileGrids = TileGrid[viewIndex, layerIndex]
        if (tileGrids.isEmpty) return
        var gridIndex = tileGrids.nextSetBit(0)
        while (gridIndex >= 0) {
            val tileGrid = TileGrid[gridIndex]
            if (!tileGrid.active) continue

            if (tileGrid.renderer == this) {
                val iterator = tileGrid.tileGridIterator(clip)
                Engine.graphics.applyViewportOffset(-tileGrid.position.x, -tileGrid.position.y)
                while (iterator.hasNext()) {
                    val index = iterator.next()
                    transformCollector(ETransform[index].renderData)
                    transformCollector + iterator.worldPosition
                    graphics.renderSprite(ETile[index].renderData, transformCollector.data)
                }
                Engine.graphics.applyViewportOffset(tileGrid.position.x, tileGrid.position.y)
            }
            gridIndex = tileGrids.nextSetBit(gridIndex + 1)
        }
    }

    override fun render(entities: DynIntArray) =
        throw UnsupportedOperationException()
}