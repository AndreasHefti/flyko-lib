package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.entity.EMultiplier
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.util.geom.Rectangle

class MultiPositionSpriteRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        entity.aspects.include(MATCHING_ASPECTS)

    override fun render(viewIndex: Int, layerIndex: Int, clip: Rectangle) {
        val toRender = getIfNotEmpty(viewIndex, layerIndex) ?: return

        val graphics = FFContext.graphics
        var i = 0
        while (i < toRender.capacity) {
            val entity = toRender[i++] ?: continue
            val sprite = entity[ESprite]
            val transform = entity[ETransform]
            val multiplier = entity[EMultiplier]
            transformCollector(transform.data)

            val pi = multiplier.positions.iterator()
            while (pi.hasNext()) {
                val x = pi.nextFloat()
                val y = pi.nextFloat()
                transformCollector.move(x, y)
                graphics.renderSprite(sprite.spriteRenderable, transformCollector.data)
                transformCollector.move(-x, -y)
            }
        }
    }

    companion object : SingletonComponent<Renderer, MultiPositionSpriteRenderer>(Renderer, MultiPositionSpriteRenderer::class) {
        override fun create() = MultiPositionSpriteRenderer()
        private val MATCHING_ASPECTS = EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, ESprite, EMultiplier
        )
    }
}