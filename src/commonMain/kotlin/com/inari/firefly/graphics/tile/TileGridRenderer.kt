package com.inari.firefly.graphics.tile

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector4i

object SimpleTileGridRenderer : EntityRenderer("SimpleTileGridRenderer") {
    override fun acceptEntity(entity: Entity) = false

    override fun sort(entities: DynArray<Entity>) {
        // not sorting
    }

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val graphics = Engine.graphics
        val tileGrids = TileGrid[viewIndex, layerIndex]
        if (tileGrids.isEmpty) return

        var tileGridIndex = tileGrids.nextSetBit(0)
        while (tileGridIndex >= 0) {
            val tileGrid = TileGrid[tileGridIndex]
            if (!tileGrid.active) continue
            if (tileGrid.viewIndex != viewIndex || tileGrid.layerIndex != layerIndex) continue
                val iterator = tileGrid.tileGridIterator(clip)
                while (iterator.hasNext()) {
                    graphics.renderSprite(
                        Entity[iterator.next()][ETile],
                        iterator.worldXPos,
                        iterator.worldYPos
                    )
                }
            tileGridIndex = tileGrids.nextSetBit(tileGridIndex + 1)
        }
    }

    override fun render(entities: DynArray<Entity>) =
        throw UnsupportedOperationException()
}

object FullTileGridRenderer : EntityRenderer("FullTileGridRenderer") {

    override fun acceptEntity(entity: Entity) = false

    override fun sort(entities: DynArray<Entity>) {
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
                while (iterator.hasNext()) {
                    val entity = Entity[iterator.next()]

                    transformCollector(entity[ETransform])
                    transformCollector + iterator.worldPosition
                    graphics.renderSprite(entity[ETile], transformCollector.data)
                }
            }
            gridIndex = tileGrids.nextSetBit(gridIndex + 1)
        }
    }

    override fun render(entities: DynArray<Entity>) =
        throw UnsupportedOperationException()
}