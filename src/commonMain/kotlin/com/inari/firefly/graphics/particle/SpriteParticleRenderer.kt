package com.inari.firefly.graphics.particle

import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray

object SpriteParticleRenderer : EntityRenderer("SpriteParticleRenderer") {

    override fun acceptEntity(index: EntityIndex)  =
        Entity[index].include(MATCHING_ASPECTS) &&
                EParticle[index].renderer == this

    override fun sort(entities: DynIntArray) {
        // no sorting
    }

    val graphics = Engine.graphics
    override fun render(entities:DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            val spriteParticle = EParticle[index].particles
            val transform = ETransform[index]
            val iterP = spriteParticle.iterator()
            while (iterP.hasNext()) {
                val particle = iterP.next()
                if (particle !is SpriteParticle)
                    continue

                transformCollector(transform.renderData)
                transformCollector + particle
                graphics.renderSprite(particle.renderData, transformCollector.data)
            }

            i = entities.nextListIndex(i + 1)
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, EParticle
    )
}