package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EMultiplier
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object MultiPositionSpriteRenderer : EntityRenderer("MultiPositionSpriteRenderer") {

    init { order = 30 }

    override fun acceptEntity(entity: Entity) = entity.aspects.include(MATCHING_ASPECTS)

    override fun sort(entities: DynArray<Entity>) {
        // no sorting
    }

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++] ?: continue
            val sprite = entity[ESprite]
            val transform = entity[ETransform]
            val multiplier = entity[EMultiplier]
            transformCollector(transform.renderData)

            val pi = multiplier.positions.iterator()
            while (pi.hasNext()) {
                val x = pi.next()
                val y = pi.next()
                transformCollector.move(x, y)
                graphics.renderSprite(sprite.renderData, transformCollector.data)
                transformCollector.move(-x, -y)
            }
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite, EMultiplier
    )
}