package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGridSystem
import com.inari.util.geom.Vector4i

class FullTileGridRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        entity.components.include(MATCHING_ASPECTS)

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val tileGridList = TileGridSystem[viewIndex, layerIndex] ?: return

        tileGridList.forEach {  tileGrid ->
            if (tileGrid.rendererRef < 0 || tileGrid.rendererRef == index) {
                val graphics = FFContext.graphics
                val iterator = tileGrid.tileGridIterator(clip)
                while (iterator.hasNext()) {
                    val entity = EntitySystem.entities[iterator.next()]

                    transformCollector(entity[ETransform].data)
                    transformCollector + iterator.worldPosition
                    graphics.renderSprite(entity[ETile].spriteRenderable, transformCollector.data)
                }
            }
        }
    }

    override fun componentType() = Companion
    companion object : SingletonComponent<Renderer, FullTileGridRenderer>(Renderer, FullTileGridRenderer::class) {
        override fun create() = FullTileGridRenderer()
        private val MATCHING_ASPECTS = EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, ETile
        )
    }
}