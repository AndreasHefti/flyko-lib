package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.Asset
import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.BindingIndex
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.SpriteData
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Sprite private constructor(): Asset(Sprite) {

    @JvmField val spriteData = SpriteData()
    @JvmField val textureRef = CReference(Texture)

    var textureIndex: BindingIndex
        get() = spriteData.textureIndex
        internal set(value) { spriteData.textureIndex = value }
    val textureBounds: Vector4i
        get() = spriteData.textureBounds
    var hFlip: Boolean
        get() = spriteData.hFlip
        set(value) { spriteData.hFlip = value }
    var vFlip: Boolean
        get() = spriteData.vFlip
        set(value) { spriteData.vFlip = value }

    override fun load() {
        super.load()
        if (assetIndex > NULL_BINDING_INDEX) return
        // ensure texture is defined
        if (!textureRef.exists)
            throw IllegalStateException("No texture defined. Define texture for sprite first")
        val tex = Texture[textureRef]
        // ensure texture is loaded otherwise load it first
        if (!tex.loaded)
            Texture.load(textureRef.targetKey)
        textureIndex = tex.assetIndex
        assetIndex = Engine.graphics.createSprite(this.spriteData)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeSprite(assetIndex)
        assetIndex = NULL_BINDING_INDEX
        super.dispose()
    }

    companion object : ComponentSubTypeBuilder<Asset, Sprite>(Asset,"Sprite") {
        override fun create() = Sprite()
    }
}