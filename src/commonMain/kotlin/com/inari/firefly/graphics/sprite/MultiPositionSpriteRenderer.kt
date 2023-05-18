package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EMultiplier
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray

object MultiPositionSpriteRenderer : EntityRenderer("MultiPositionSpriteRenderer") {

    init { order = 30 }

    override fun acceptEntity(index: EntityIndex) =
        Entity[index].include(MATCHING_ASPECTS)


    override fun sort(entities: DynIntArray) {
        // no sorting
    }

    private val graphics = Engine.graphics
    override fun render(entities: DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            val sprite = ESprite[index]
            val transform = ETransform[index]
            val multiplier = EMultiplier[index]
            transformCollector(transform.renderData)

            val pi = multiplier.positions.iterator()
            while (pi.hasNext()) {
                val x = pi.next()
                val y = pi.next()
                transformCollector.move(x, y)
                graphics.renderSprite(sprite.renderData, transformCollector.data)
                transformCollector.move(-x, -y)
            }

            i = entities.nextListIndex(i + 1)
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite, EMultiplier
    )
}