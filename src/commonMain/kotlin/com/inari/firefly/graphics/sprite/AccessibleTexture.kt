package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.SubComponentBuilder

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

    companion object : SubComponentBuilder<Asset, AccessibleTexture>(Asset) {
        override fun create() = AccessibleTexture()
    }
}