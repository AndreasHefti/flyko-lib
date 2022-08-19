package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.EChild
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent.Companion.ENTITY_COMPONENT_ASPECTS
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.tile.ETile
import com.inari.util.geom.Vector4i

class SimpleSpriteRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        entity.components.include(MATCHING_ASPECTS) &&
            entity.components.exclude(EXCLUDING_ASPECTS)

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val toRender = getIfNotEmpty(viewIndex, layerIndex) ?: return

        val graphics = FFContext.graphics
        var i = 0
        while (i < toRender.capacity) {
            val entity = toRender[i++] ?: continue
            graphics.renderSprite(
                entity[ESprite].spriteRenderable,
                entity[ETransform].data
            )
        }
    }

    override fun componentType() = Companion
    companion object : SingletonComponent<Renderer, SimpleSpriteRenderer>(Renderer, SimpleSpriteRenderer::class) {
        override fun create() = SimpleSpriteRenderer()
        private val MATCHING_ASPECTS = ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, ESprite
        )
        private val EXCLUDING_ASPECTS = ENTITY_COMPONENT_ASPECTS.createAspects(
            EChild, ETile
        )
    }
}