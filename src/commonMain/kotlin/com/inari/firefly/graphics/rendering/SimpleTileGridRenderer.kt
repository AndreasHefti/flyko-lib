package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.tile.ETile
import com.inari.firefly.graphics.tile.TileGridSystem
import com.inari.util.geom.Rectangle

class SimpleTileGridRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        false

    override fun render(viewIndex: Int, layerIndex: Int, clip: Rectangle) {
        val tileGrid = TileGridSystem[viewIndex, layerIndex] ?: return
        if (tileGrid.rendererRef < 0 || tileGrid.rendererRef == index) {
            val graphics = FFContext.graphics
            val iterator = tileGrid.tileGridIterator(clip)
            while (iterator.hasNext()) {
                graphics.renderSprite(
                    EntitySystem.entities[iterator.next()][ETile].spriteRenderable,
                    iterator.worldXPos,
                    iterator.worldYPos
                )
            }
        }
    }

    override fun componentType() = Companion
    companion object : SingletonComponent<Renderer, SimpleTileGridRenderer>(Renderer, SimpleTileGridRenderer::class) {
        override fun create() = SimpleTileGridRenderer()
        private val MATCHING_ASPECTS = EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, ETile
        )
    }
}