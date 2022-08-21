package com.inari.firefly.graphics.text

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Font private constructor(): Asset() {

    @JvmField val textureRef = CReference(Texture)
    @JvmField internal var dChar = -1
    @JvmField internal val charSpriteMap = DynIntArray(256, -1)
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

    private val tmpSpriteData = object : SpriteData {
        override var textureIndex = -1
        override val region = Vector4i()
        override val isHorizontalFlip = false
        override val isVerticalFlip = false
    }

    override fun notifyParent(comp: Component) = textureRef(comp.name)

    override fun load() {
        tmpSpriteData.textureIndex = resolveAssetIndex(textureRef.targetKey)
        tmpSpriteData.region(0, 0, charWidth, charHeight)
        for (y in charMap.indices) {
            for (x in charMap[y].indices) {
                tmpSpriteData.region(
                    x * charWidth + xOffset,
                    y * charHeight + yOffset)

                val charSpriteId = Engine.graphics.createSprite(tmpSpriteData)
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
            Engine.graphics.disposeSprite(iterator.next())

        charSpriteMap.clear()
        Engine.graphics.disposeTexture(tmpSpriteData.textureIndex)
        tmpSpriteData.textureIndex = -1
    }

    companion object :  ComponentSubTypeSystem<Asset, Font>(Asset) {
        override fun create() = Font()
    }
}