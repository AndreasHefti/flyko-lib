package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentSubTypeSystem
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.TextureData
import com.inari.util.NO_NAME
import com.inari.util.NULL_INT_FUNCTION
import com.inari.util.ZERO_INT

class Texture private constructor() : Asset(), TextureData {

    var width: Int = -1
        private set
    var height: Int = -1
        private set

    override var resourceName = NO_NAME
        set(value) { field = checkNotLoaded(value, "ResourceName") }
    override var isMipmap = false
        set(value) { field = checkNotLoaded(value, "MipMap") }
    override var wrapS = -1
        set(value) { field = checkNotLoaded(value, "WrapS") }
    override var wrapT = -1
        set(value) { field = checkNotLoaded(value, "WrapT") }
    override var minFilter = -1
        set(value) { field = checkNotLoaded(value, "MinFilter") }
    override var magFilter = -1
        set(value) { field = checkNotLoaded(value, "MagFilter") }
    override var colorConverter = NULL_INT_FUNCTION
        set(value) { field = checkNotLoaded(value, "ColorConverter") }

    override fun load() {
        if (assetIndex >= 0) return
        val textData = Engine.graphics.createTexture(this)
        assetIndex = textData.first
        width = textData.second
        height = textData.third
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeTexture(assetIndex)
        assetIndex = -1
        width = -1
        height = -1
    }

    companion object :  ComponentSubTypeSystem<Asset, Texture>(Asset) {
        override fun create() = Texture()
    }
}