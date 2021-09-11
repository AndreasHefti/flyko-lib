package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.EChild
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.util.geom.Rectangle

class SpriteGroupRenderer private constructor() : Renderer(
    sort = { entities -> entities.sort(COMPARATOR) }
) {

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
            val group = entity[EChild]

            transformCollector(transform.data)
            collectTransformData(group.int_parent, transformCollector)
            graphics.renderSprite(sprite.spriteRenderable, transformCollector.data)
        }
    }

    companion object : SingletonComponent<Renderer, SpriteGroupRenderer>(Renderer, SpriteGroupRenderer::class) {
        private val COMPARATOR = Comparator<Entity?> { e1, e2 ->
            e1?.get(EChild)?.zPos ?: 0.compareTo(e2?.get(EChild)?.zPos ?: 0)
        }

        override fun create() = SpriteGroupRenderer()

        private val MATCHING_ASPECTS = EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, ESprite, EChild
        )
    }
}