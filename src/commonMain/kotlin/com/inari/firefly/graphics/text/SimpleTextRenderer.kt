package com.inari.firefly.graphics.text

import com.inari.firefly.core.EChild
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray

object SimpleTextRenderer : EntityRenderer("SimpleTextRenderer") {

    init { order = 50 }

    private val textRenderable = SpriteRenderable()

    override fun acceptEntity(entity: Entity) =
        entity.aspects.include(MATCHING_ASPECTS) &&
                entity[EText].renderer == this


    override fun sort(entities: DynArray<Entity>) {
        // no sorting here
    }

    override fun render(entities: DynArray<Entity>) {
        val graphics = Engine.graphics
        var i = 0
        while (i < entities.capacity) {
            val entity = entities[i++] ?: continue

            val text = entity[EText]
            val transform = entity[ETransform]
            val metadata = if (entity.has(ETextMeta)) entity[ETextMeta] else null
            val font = Font[text.fontRef.targetKey]
            val chars = text.text

            textRenderable.tintColor(text.tint)
            textRenderable.blendMode = text.blend
            transformCollector(transform.renderData)
            if (EChild in entity.aspects)
                collectTransformData(entity[EChild].parent.targetKey.componentIndex, transformCollector)

            val horizontalStep = (font.charWidth + font.charSpace) * transform.scale.v0
            val verticalStep = (font.charHeight + font.lineSpace) * transform.scale.v1
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

                textRenderable.spriteIndex = font.charSpriteMap[char.code]

                if (textRenderable.spriteIndex < 0) {
                    transformCollector.data.position.x += horizontalStep
                    continue
                }

                val charData = metadata?.resolver?.invoke(j - 1)
                charData?.also {
                    transformCollector + it.transformData
                    textRenderable.blendMode = it.blend
                    textRenderable.tintColor(it.tint)
                }

                graphics.renderSprite(textRenderable, transformCollector.data)
                charData?.also {
                    transformCollector - it.transformData.position
                    transformCollector.data.pivot(transform.pivot)
                    transformCollector.data.rotation = transform.rotation
                    transformCollector.data.scale(transform.scale)
                    textRenderable.blendMode = text.blend
                    textRenderable.tintColor(text.tint)
                }
                transformCollector.data.position.x += horizontalStep
            }
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, EText
    )
}