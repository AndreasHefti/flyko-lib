package com.inari.firefly.core

abstract class Asset protected constructor(subType: ComponentType<out Asset>) : ComponentNode(subType) {

    var assetIndex: Int = -1
        protected set

    open fun assetIndex(at: Int) = assetIndex

    companion object : ComponentSystem<Asset>("Asset") {
        override fun allocateArray(size: Int): Array<Asset?> = arrayOfNulls(size)
        override fun create(): Asset =
            throw UnsupportedOperationException("Asset is abstract use a concrete implementation instead")

        fun resolveAssetIndex(key: ComponentKey, preload: Boolean = true, setIndex: Int = 0): Int {
            if (key == NO_COMPONENT_KEY)
                return -1
            if (key.type.aspectIndex != Asset.aspectIndex)
                throw IllegalArgumentException("Type mismatch")
            if (key.instanceIndex < 0)
                throw IllegalArgumentException("No instance")
            return resolveAssetIndex(key.instanceIndex, preload, setIndex)
        }

        fun resolveAssetIndex(assetIndex: Int, preload: Boolean = true, setIndex: Int = 0): Int {
            val asset = this[assetIndex]
            if (!asset.loaded && preload)
                load(assetIndex)
            return asset.assetIndex(setIndex)
        }
    }
}