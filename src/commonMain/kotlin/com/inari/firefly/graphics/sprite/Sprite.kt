package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.util.ZERO_INT
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Sprite private constructor(): Asset(Sprite), SpriteData {

    override val textureIndex: Int
        get() =  resolveAssetIndex(textureRef.targetKey)

    @JvmField val textureRef = CReference(Texture)
    val textureRegion: Vector4i
        get() = textureBounds
    override val textureBounds: Vector4i = Vector4i(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
    override var hFlip: Boolean = false
    override var vFlip: Boolean = false

    override fun setParentComponent(key: ComponentKey) {
        super.setParentComponent(key)
        if (key.type.aspectIndex == Asset.aspectIndex)
            textureRef(key)
        else
            textureRef.reset()
    }

    override fun load() {
        super.load()
        if (assetIndex >= 0) return
        assetIndex = Engine.graphics.createSprite(this)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeSprite(assetIndex)
        assetIndex = -1
        super.dispose()
    }

    companion object :  ComponentSubTypeSystem<Asset, Sprite>(Asset, "Sprite") {
        override fun create() = Sprite()

    }
}