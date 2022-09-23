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

        override fun clearSystem() {
            super.clearSystem()
            loadSystemFont()
        }

        private fun loadSystemFont() {
            Texture.build {
                name = Engine.SYSTEM_FONT_ASSET
                resourceName = "firefly/fireflyMicroFont.png"
            }

            Font.build {
                name = Engine.SYSTEM_FONT
                textureRef(Engine.SYSTEM_FONT_ASSET)
                charWidth = 8
                charHeight = 16
                charSpace = 0
                lineSpace = 0
                defaultChar = 'a'
                charMap = arrayOf(
                    charArrayOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',' '),
                    charArrayOf('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',' '),
                    charArrayOf('1','2','3','4','5','6','7','8','9','0','!','@','£','$','%','?','&','*','(',')','-','+','=','"','.',',',':')
                )
            }
            Font.activate(Engine.SYSTEM_FONT)
        }
    }
}