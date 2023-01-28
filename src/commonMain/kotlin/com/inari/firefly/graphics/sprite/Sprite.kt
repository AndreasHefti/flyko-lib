package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.util.ZERO_INT
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Sprite private constructor(): Asset(Sprite), SpriteData {

    override var textureIndex: Int = -1
        internal set
    @JvmField val textureRef = CReference(Texture)
    val textureRegion: Vector4i
        get() = textureBounds
    override val textureBounds: Vector4i = Vector4i(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
    override var hFlip: Boolean = false
    override var vFlip: Boolean = false

    override fun load() {
        super.load()
        if (assetIndex >= 0) return
        // ensure texture is defined
        if (!textureRef.exists)
            throw IllegalStateException("No texture defined. Define texture for sprite first")
        val tex = Texture[textureRef]
        // ensure texture is loaded otherwise load it first
        if (!tex.loaded)
            Texture.load(textureRef.targetKey)
        textureIndex = tex.assetIndex
        assetIndex = Engine.graphics.createSprite(this)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeSprite(assetIndex)
        assetIndex = -1
        super.dispose()
    }

    companion object : ComponentSubTypeBuilder<Asset, Sprite>(Asset,"Sprite") {
        override fun create() = Sprite()
    }
}