package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.SpriteData
import com.inari.util.ZERO_INT
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Sprite private constructor(): Asset(), SpriteData {

    override var textureIndex: Int = -1
        internal set

    @JvmField val textureRef = CReference(Texture)
    override val region: Vector4i = Vector4i(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
    override var isHorizontalFlip: Boolean = false
    override var isVerticalFlip: Boolean = false

    override fun notifyParent(comp: Component) {
        textureRef(comp.name)
    }

    override fun load() {
        if (assetIndex >= 0) return
        textureIndex = resolveAssetIndex(textureRef.targetKey)
        assetIndex = Engine.graphics.createSprite(this)
    }

    override fun dispose() {
        if (assetIndex < 0) return
        Engine.graphics.disposeSprite(assetIndex)
        assetIndex = -1
    }

    companion object :  ComponentSubTypeSystem<Asset, Sprite>(Asset) {
        override fun create() = Sprite()
    }
}