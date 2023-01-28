package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine

class AccessibleTexture private constructor() : Texture() {

    var pixels: ByteArray? = null
        internal set

    override fun load() {
        super.load()
        pixels = Engine.graphics.getTexturePixels(assetIndex)
    }

    override fun dispose() {
        pixels = null
        super.dispose()
    }

    companion object : ComponentSubTypeBuilder<Asset, AccessibleTexture>(Asset,"BitmaskTexture") {
        override fun create() = AccessibleTexture()
    }
}