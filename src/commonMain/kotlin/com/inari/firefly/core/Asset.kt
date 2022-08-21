package com.inari.firefly.core

import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.text.Font

abstract class Asset  protected constructor() : Component(Asset) {

    var assetIndex: Int = -1
        protected set

    open fun assetIndex(at: Int) = assetIndex

    override val componentType = Companion
    companion object : ComponentSystem<Asset>("Asset") {
        override fun allocateArray(size: Int): Array<Asset?> = arrayOfNulls(size)
        override fun create(): Asset =
            throw UnsupportedOperationException("Asset is abstract use a concrete implementation instead")

        fun resolveAssetIndex(key: ComponentKey, preload: Boolean = true): Int {
            if (key == NO_COMPONENT_KEY)
                return -1
            if (key.type.aspectIndex != Asset.aspectIndex)
                throw IllegalArgumentException("Type mismatch")
            if (key.instanceId < 0)
                throw IllegalArgumentException("No instance")
            return resolveAssetIndex(key.instanceId, preload)
        }

        fun resolveAssetIndex(assetIndex: Int, preload: Boolean = true): Int {
            val asset = this[assetIndex]
            if (!asset.loaded && preload)
                load(assetIndex)
            return asset.assetIndex
        }
    }
}