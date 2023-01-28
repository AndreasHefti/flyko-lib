package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.TextureData
import com.inari.firefly.game.tile.TileSet
import com.inari.firefly.graphics.text.Font
import com.inari.util.NO_NAME
import com.inari.util.NULL_INT_FUNCTION

open class Texture protected constructor() : Asset(Texture), TextureData {

    var width: Int = -1
        private set
    var height: Int = -1
        private set

    override var resourceName = NO_NAME
        set(value) { field = checkNotLoaded(value, "ResourceName") }
    override var isMipmap = false
        set(value) { field = checkNotLoaded(value, "MipMap") }
    override var wrapS = -1
        set(value) { field = checkNotLoaded(value, "WrapS") }
    override var wrapT = -1
        set(value) { field = checkNotLoaded(value, "WrapT") }
    override var minFilter = -1
        set(value) { field = checkNotLoaded(value, "MinFilter") }
    override var magFilter = -1
        set(value) { field = checkNotLoaded(value, "MagFilter") }
    override var colorConverter = NULL_INT_FUNCTION
        set(value) { field = checkNotLoaded(value, "ColorConverter") }

    fun withSprite(configure: (Sprite.() -> Unit)): ComponentKey {
        val sprite = Sprite.buildAndGet(configure)
        sprite.textureRef(earlyKeyAccess())
        return sprite.key
    }

    fun withSpriteSet(configure: (SpriteSet.() -> Unit)): ComponentKey {
        val spriteSet = SpriteSet.buildAndGet(configure)
        spriteSet.textureRef(earlyKeyAccess())
        return spriteSet.key
    }

    fun withFont(configure: (Font.() -> Unit)): ComponentKey {
        val font = Font.buildAndGet(configure)
        font.textureRef(earlyKeyAccess())
        return font.key
    }

    fun withTileSet(configure: (TileSet.() -> Unit)): ComponentKey {
        val tileSet = TileSet.buildAndGet(configure)
        tileSet.textureRef(earlyKeyAccess())
        return tileSet.key
    }

    override fun load() {
        if (assetIndex >= 0) return
        val textData = Engine.graphics.createTexture(this)
        assetIndex = textData.first
        width = textData.second
        height = textData.third
    }

    override fun dispose() {
        // dispose all child assets first
        Asset.forEachDo {
            if (it is Sprite && it.textureRef.targetKey == this.key) Sprite.dispose(it)
            else if (it is SpriteSet && it.textureRef.targetKey == this.key) SpriteSet.dispose(it)
            else if (it is Font && it.textureRef.targetKey == this.key) Font.dispose(it)
        }
        TileSet.forEachDo {
            if (it.textureRef.targetKey == this.key) TileSet.dispose(it)
        }

        if (assetIndex < 0) return
        Engine.graphics.disposeTexture(assetIndex)
        assetIndex = -1
        width = -1
        height = -1
    }

    override fun delete() {
        // delete all child assets first
        Asset.forEachDo {
            if (it is Sprite && it.textureRef.targetKey == this.key) Sprite.delete(it)
            else if (it is SpriteSet && it.textureRef.targetKey == this.key) SpriteSet.delete(it)
            else if (it is Font && it.textureRef.targetKey == this.key) Font.delete(it)
        }
        TileSet.forEachDo {
            if (it.textureRef.targetKey == this.key) TileSet.delete(it)
        }

        super.delete()
    }

    companion object : ComponentSubTypeBuilder<Asset, Texture>(Asset,"Texture") {
        override fun create() = Texture()
    }
}