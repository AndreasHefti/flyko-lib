package com.inari.firefly.graphics.text

import com.inari.firefly.core.Asset
import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.core.api.SpriteData
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.collection.DynIntArray
import kotlin.jvm.JvmField

class Font private constructor(): Asset(Font) {

    @JvmField val textureRef = CReference(Texture)
    @JvmField internal var dChar = -1
    @JvmField internal val charSpriteMap = DynIntArray(256, NULL_COMPONENT_INDEX)
    @JvmField var charMap: Array<CharArray> = emptyArray()
    @JvmField var charWidth = 0
    @JvmField var charHeight = 0
    @JvmField var charSpace = 0
    @JvmField var lineSpace = 0
    @JvmField var xOffset = 0
    @JvmField var yOffset = 0
    var defaultChar : Char
        get() = dChar.toChar()
        set(value) { dChar = value.code }

    private val spriteData = SpriteData()

    override fun load() {
        spriteData.textureIndex = resolveAssetIndex(textureRef.targetKey)
        spriteData.textureBounds(0, 0, charWidth, charHeight)
        for (y in charMap.indices) {
            for (x in charMap[y].indices) {
                spriteData.textureBounds(
                    x * charWidth + xOffset,
                    y * charHeight + yOffset)

                val charSpriteId = Engine.graphics.createSprite(spriteData)
                charSpriteMap[charMap[y][x].code] = charSpriteId
            }
        }
    }

    operator fun get(char: Char): Int {
        val index = char.code
        return if (charSpriteMap.isEmpty(index))
            dChar
        else
            charSpriteMap[index]
    }

    override fun dispose() {
        val iterator = charSpriteMap.iterator()
        while (iterator.hasNext())
            Engine.graphics.disposeSprite(iterator.nextInt())

        charSpriteMap.clear()
        Engine.graphics.disposeTexture(spriteData.textureIndex)
        spriteData.textureIndex = NULL_BINDING_INDEX
    }

    companion object : ComponentSubTypeBuilder<Asset, Font>(Asset,"Font") {
        override fun create() = Font()
    }
}