package com.inari.firefly.graphics.text

import com.inari.firefly.core.EChild
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynIntArray

object SimpleTextRenderer : EntityRenderer("SimpleTextRenderer") {

    init { order = 50 }

    private val textRenderable = SpriteRenderable()

    override fun acceptEntity(index: EntityIndex) =
        Entity[index].include(MATCHING_ASPECTS) &&
                EText[index].renderer == this


    override fun sort(entities: DynIntArray) {
        // no sorting here
    }

    val graphics = Engine.graphics
    override fun render(entities: DynIntArray) {
        var i = entities.nextListIndex(0)
        while (i >= ZERO_INT) {
            val index = entities[i]
            val text = EText[index]
            val transform = ETransform[index]
            val metadata: ETextMeta? = ETextMeta.getIfExists(index)
            val child = EChild.getIfExists(index)
            val font = Font[text.fontRef.targetKey]
            val chars = text.text

            textRenderable.tintColor(text.tint)
            textRenderable.blendMode = text.blend
            transformCollector(transform.renderData)
            if (child != null)
                collectTransformData(child.parent.targetKey.componentIndex, transformCollector)

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

            i = entities.nextListIndex(i + 1)
        }
    }

    private val MATCHING_ASPECTS = Entity.ENTITY_COMPONENT_ASPECTS.createAspects(
        ETransform, EText
    )
}