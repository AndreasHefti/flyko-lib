package com.inari.firefly.graphics.rendering

import com.inari.firefly.FFContext
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.entity.EChild
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.graphics.text.ETextMeta
import com.inari.firefly.graphics.text.FontAsset
import com.inari.util.geom.Rectangle


class SimpleTextRenderer private constructor() : Renderer() {

    override fun match(entity: Entity): Boolean =
        entity.components.include(MATCHING_ASPECTS) &&
            entity[EText].rendererRef == index

    override fun render(viewIndex: Int, layerIndex: Int, clip: Rectangle) {
        val toRender = getIfNotEmpty(viewIndex, layerIndex) ?: return

        val graphics = FFContext.graphics
        var i = 0
        while (i < toRender.capacity) {
            val entity = toRender[i++] ?: continue

            val text = entity[EText]
            val transform = entity[ETransform]
            val metadata = if (entity.has(ETextMeta)) entity[ETextMeta] else null
            val font = FFContext[FontAsset, text.fontAssetRef]
            val chars = text.text

            textRenderable.tintColor(text.tint)
            textRenderable.blendMode = text.blend
            transformCollector(transform.data)
            if (EChild in entity.aspects)
                collectTransformData(entity[EChild].int_parent, transformCollector)

            val horizontalStep = (font.charWidth + font.charSpace) * transform.data.scale.v0
            val verticalStep = (font.charHeight + font.lineSpace) * transform.data.scale.v1
            val newLinePos = transformCollector.data.position.x
            var j = 0
            while (j < chars.length) {
                val char = chars[j++]
                if (char == '\n') {
                    transformCollector.data.position.x = newLinePos
                    transformCollector.data.position.y += verticalStep
                    continue
                }

                if (char == ' ') {
                    transformCollector.data.position.x += horizontalStep
                    continue
                }

                textRenderable.spriteId = font.charSpriteMap[char.toInt()]

                if (textRenderable.spriteId < 0) {
                    transformCollector.data.position.x += horizontalStep
                    continue
                }

                val charData = metadata?.resolver?.invoke(j - 1)
                charData?.also {
                    transformCollector + it.transformData
                    textRenderable.blendMode = it.blend
                    textRenderable.tintColor = it.tint
                }

                graphics.renderSprite(textRenderable, transformCollector.data)
                charData?.also {
                    transformCollector - it.transformData.position
                    transformCollector.data.pivot(transform.pivot)
                    transformCollector.data.rotation = transform.rotation
                    transformCollector.data.scale(transform.scale)
                    textRenderable.blendMode = text.blend
                    textRenderable.tintColor = text.tint
                }
                transformCollector.data.position.x += horizontalStep
            }
        }
    }

    private val textRenderable = SpriteRenderable()

    override fun componentType() = Companion
    companion object : SingletonComponent<Renderer, SimpleTextRenderer>(Renderer, SimpleTextRenderer::class) {
        override fun create() = SimpleTextRenderer()
        private val MATCHING_ASPECTS = EntityComponent.ENTITY_COMPONENT_ASPECTS.createAspects(
            ETransform, EText
        )
    }
}