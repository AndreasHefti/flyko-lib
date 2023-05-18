package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EChild
import com.inari.firefly.core.EMultiplier
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.Entity.Companion.ENTITY_COMPONENT_ASPECTS
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynIntArray

object SimpleSpriteRenderer : EntityRenderer("SimpleSpriteRenderer") {

    init { order = 35 }

    override fun acceptEntity(index: EntityIndex): Boolean {
        val entity = Entity[index]
        return entity.include(MATCHING_ASPECTS) && entity.exclude(EXCLUDING_ASPECTS)
    }

    override fun sort(entities: DynIntArray) {
        // no sorting
    }

    private val graphics = Engine.graphics
    override fun render(entities: DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            graphics.renderSprite(ESprite[index].renderData, ETransform[index].renderData)
            i = entities.nextListIndex(i + 1)
        }
    }

    private val MATCHING_ASPECTS = ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite
    )
    private val EXCLUDING_ASPECTS =ENTITY_COMPONENT_ASPECTS.createAspects(
        EChild, EMultiplier
    )
}