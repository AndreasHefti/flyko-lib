package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.EChild
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object SpriteGroupRenderer : EntityRenderer("SpriteGroupRenderer") {

    init { order = 32 }

    override fun acceptEntity(entity: Entity) = entity.aspects.include(MATCHING_ASPECTS)
    override fun sort(entities: DynArray<Entity>) = entities.sort(COMPARATOR)

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++] ?: continue

            val sprite = entity[ESprite]
            val transform = entity[ETransform]
            val group = entity[EChild]

            transformCollector(transform)
            collectTransformData(group.parentIndex, transformCollector)
            graphics.renderSprite(sprite, transformCollector.data)
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, ESprite, EChild
    )

    private val COMPARATOR = Comparator<Entity?> { e1, e2 ->
        e1?.get(EChild)?.zPos ?: 0.compareTo(e2?.get(EChild)?.zPos ?: 0)
    }
}