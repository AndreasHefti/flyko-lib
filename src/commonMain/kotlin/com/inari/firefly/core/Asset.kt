package com.inari.firefly.core

import com.inari.firefly.core.api.BindingIndex
import com.inari.firefly.core.api.NULL_BINDING_INDEX

abstract class Asset protected constructor(assetType: ComponentType<out Asset>) : Component(assetType) {

    var assetIndex: BindingIndex = NULL_BINDING_INDEX
        protected set
    open fun assetIndex(at: Int): Int = assetIndex

    companion object : AbstractComponentSystem<Asset>("Asset") {
        override fun allocateArray(size: Int): Array<Asset?> = arrayOfNulls(size)

        fun resolveAssetIndex(key: ComponentKey, preload: Boolean = true, setIndex: Int = 0): Int {
            if (key == NO_COMPONENT_KEY)
                throw IllegalArgumentException("Missing Asset reference: $key")
            if (key.type.aspectIndex != Asset.aspectIndex)
                throw IllegalArgumentException("Type mismatch")
            if (key.componentIndex < 0)
                throw IllegalArgumentException("No instance")

            val asset: Asset = ComponentSystem[key]
            if (!asset.loaded && preload)
                ComponentSystem.load(key)

            return asset.assetIndex(setIndex)
        }
    }
}