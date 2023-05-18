package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EChild
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray

object SpriteGroupRenderer : EntityRenderer("SpriteGroupRenderer") {

    init { order = 32 }

    override fun acceptEntity(index: EntityIndex) = Entity[index].include(MATCHING_ASPECTS)
    override fun sort(entities: DynIntArray) = entities.sort(COMPARATOR)

    val graphics = Engine.graphics
    override fun render(entities: DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            val sprite = ESprite[index]
            val transform = ETransform[index]
            val group = EChild[index]

            transformCollector(transform.renderData)
            collectTransformData(group.parentIndex, transformCollector)
            graphics.renderSprite(sprite.renderData, transformCollector.data)

            i = entities.nextListIndex(i + 1)
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite, EChild
    )

    private val COMPARATOR = Comparator<Int> { i1, i2 ->
        EChild.components[i1]?.zPos?: 0.compareTo(EChild.components[i2]?.zPos ?: 0)
    }
}