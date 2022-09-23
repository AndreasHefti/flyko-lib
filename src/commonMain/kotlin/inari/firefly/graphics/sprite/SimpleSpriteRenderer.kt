package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EChild
import com.inari.firefly.core.EMultiplier
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.Entity.Companion.ENTITY_COMPONENT_ASPECTS
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object SimpleSpriteRenderer : EntityRenderer() {

    init {
        order = 30
    }

    override fun acceptEntity(entity: Entity): Boolean =
        entity.components.include(MATCHING_ASPECTS) && entity.components.exclude(EXCLUDING_ASPECTS)

    override fun sort(entities: DynArray<Entity>) {
        // no sorting
    }

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++] ?: continue
            graphics.renderSprite(entity[ESprite], entity[ETransform])
        }
    }

    val MATCHING_ASPECTS = ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite
    )
     val EXCLUDING_ASPECTS =ENTITY_COMPONENT_ASPECTS.createAspects(
        EChild, EMultiplier
    )
}