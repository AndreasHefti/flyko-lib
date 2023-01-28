package com.inari.firefly.core

abstract class Asset protected constructor(assetType: ComponentType<out Asset>) : Composite(assetType) {

    var assetIndex: Int = -1
        protected set
    open fun assetIndex(at: Int) = assetIndex

    companion object : ComponentSystem<Asset>("Asset") {
        override fun allocateArray(size: Int): Array<Asset?> = arrayOfNulls(size)
        override fun create() = throw UnsupportedOperationException("Trigger is abstract use sub type builder instead")

        fun resolveAssetIndex(key: ComponentKey, preload: Boolean = true, setIndex: Int = 0): Int {
            if (key == NO_COMPONENT_KEY)
                throw IllegalArgumentException("Missing Asset reference: $key")
            if (key.type.aspectIndex != Asset.aspectIndex)
                throw IllegalArgumentException("Type mismatch")
            if (key.instanceIndex < 0)
                throw IllegalArgumentException("No instance")
            val asset: Asset = ComponentSystem[key]
            if (!asset.loaded && preload)
                ComponentSystem.load(key)
            return asset.assetIndex(setIndex)
        }
    }
}