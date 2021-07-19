package com.inari.firefly.graphics

import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.api.TextureData
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class TextureAsset private constructor() : Asset() {

    @JvmField internal var id: Int = -1
    override fun instanceId(index: Int): Int = id

    var width: Int = -1
        private set
    var height: Int = -1
        private set

    @JvmField internal val textureData = TextureData()

    var resourceName: String
        get() = textureData.resourceName
        set(value) {textureData.resourceName = setIfNotInitialized(value, "ResourceName")}
    var mipMap
        get() = textureData.isMipmap
        set(value) {textureData.isMipmap = setIfNotInitialized(value, "MipMap")}
    var wrapS
        get() = textureData.wrapS
        set(value) {textureData.wrapS = setIfNotInitialized(value, "WrapS")}
    var wrapT
        get() = textureData.wrapT
        set(value) {textureData.wrapT = setIfNotInitialized(value, "WrapT")}
    var minFilter
        get() = textureData.minFilter
        set(value) {textureData.minFilter = setIfNotInitialized(value, "MinFilter")}
    var magFilter
        get() = textureData.magFilter
        set(value) {textureData.magFilter = setIfNotInitialized(value, "MagFilter")}
    var colorConverter
        get() = textureData.colorConverter
        set(value) {textureData.colorConverter = setIfNotInitialized(value, "ColorConverter")}

    override fun load() {
        if (id < 0) {
            val textData = FFContext.graphics.createTexture(textureData)
            id = textData.first
            width = textData.second
            height = textData.third
        }
    }

    override fun unload() {
        if (id >= 0) {
            FFContext.graphics.disposeTexture(id)
            id = -1
            width = -1
            height = -1
        }
    }

    companion object : SystemComponentSubType<Asset, TextureAsset>(Asset, TextureAsset::class) {
        override fun createEmpty() = TextureAsset()
    }
}