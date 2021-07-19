package com.inari.firefly.graphics.text

import com.inari.firefly.*
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.SpriteData
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.TextureAsset
import com.inari.util.collection.DynIntArray
import kotlin.jvm.JvmField


class FontAsset : Asset() {

    @JvmField internal var textureAssetId = NO_COMP_ID
    @JvmField internal var dChar = -1

    internal val charSpriteMap = DynIntArray(256, -1)
    private val tmpSpriteData = SpriteData()

    var texture = ComponentRefResolver(Asset) { index-> run {
            dependingRef = setIfNotInitialized(index, "texture")
            textureAssetId = CompId(index, TextureAsset)
        } }
    var charMap: Array<CharArray> = emptyArray()
        set(value) {field = setIfNotInitialized(value, "charMap")}
    var charWidth = 0
        set(value) {field = setIfNotInitialized(value, "charWidth")}
    var charHeight = 0
        set(value) {field = setIfNotInitialized(value, "charHeight")}
    var charSpace = 0
        set(value) {field = setIfNotInitialized(value, "charSpace")}
    var lineSpace = 0
        set(value) {field = setIfNotInitialized(value, "lineSpace")}
    var defaultChar : Char
        get() = dChar.toChar()
        set(value) {dChar = setIfNotInitialized(value.toInt(), "defaultChar")}
    var xOffset = 0
        set(value) {field = setIfNotInitialized(value, "xOffset")}
    var yOffset = 0
        set(value) {field = setIfNotInitialized(value, "yOffset")}

    override fun instanceId(index: Int): Int =
        throw UnsupportedOperationException()

    override fun load() {
        val graphics = FFContext.graphics
        FFContext.activate(textureAssetId)
        val texture: TextureAsset = FFContext[textureAssetId]

        tmpSpriteData.textureId = texture.instanceId
        tmpSpriteData.region(0, 0, charWidth, charHeight)
        for (y in charMap.indices) {
            for (x in charMap[y].indices) {
                tmpSpriteData.region(
                        x * charWidth + xOffset,
                        y * charHeight + yOffset)

                val charSpriteId = graphics.createSprite(tmpSpriteData)
                charSpriteMap[charMap[y][x].toInt()] = charSpriteId
            }
        }
    }

    operator fun get(char: Char): Int {
        val index = char.toInt()
        return if (charSpriteMap.isEmpty(index))
            dChar
        else
            charSpriteMap[index]
    }

    override fun unload() {
        val graphics = FFContext.graphics

        val iterator = charSpriteMap.iterator()
        while (iterator.hasNext()) {
            graphics.disposeSprite(iterator.next())
        }
        charSpriteMap.clear()

        graphics.disposeTexture(tmpSpriteData.textureId)
        tmpSpriteData.textureId = -1
    }

    companion object : SystemComponentSubType<Asset, FontAsset>(Asset, FontAsset::class) {
        override fun createEmpty() = FontAsset()
    }

}