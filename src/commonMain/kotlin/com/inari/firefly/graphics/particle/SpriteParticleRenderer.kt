package com.inari.firefly.graphics.particle

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object SpriteParticleRenderer : EntityRenderer("SpriteParticleRenderer") {
    override fun acceptEntity(entity: Entity)  =
        entity.aspects.include(MATCHING_ASPECTS) &&
                entity[EParticle].renderer == this

    override fun sort(entities: DynArray<Entity>) {
        // no sorting
    }

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++]
                ?: continue

            val spriteParticle = entity[EParticle].particles
            val transform = entity[ETransform]

            var ii = 0
            while (ii < spriteParticle.capacity) {
                val particle = spriteParticle[ii++]
                    ?: continue

                if (particle !is SpriteParticle)
                    continue

                transformCollector(transform)
                transformCollector + particle.transformData
                graphics.renderSprite(particle, transformCollector.data)
            }
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, EParticle
    )
}