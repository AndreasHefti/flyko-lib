package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.TextureData
import com.inari.firefly.game.world.TileSet
import com.inari.firefly.graphics.text.Font
import kotlin.jvm.JvmField

open class Texture protected constructor() : Asset(Texture) {

    @JvmField val textureData = TextureData()

    var width: Int = -1
        private set
    var height: Int = -1
        private set

    var resourceName
        get() = textureData.resourceName
        set(value) { textureData.resourceName = checkNotLoaded(value, "ResourceName") }
    var isMipmap
        get() = textureData.isMipmap
        set(value) { textureData.isMipmap = checkNotLoaded(value, "MipMap") }
    var wrapS
        get() = textureData.wrapS
        set(value) { textureData.wrapS = checkNotLoaded(value, "WrapS") }
    var wrapT
        get() = textureData.wrapT
        set(value) { textureData.wrapT = checkNotLoaded(value, "WrapT") }
    var minFilter
        get() = textureData.minFilter
        set(value) { textureData.minFilter = checkNotLoaded(value, "MinFilter") }
    var magFilter
        get() = textureData.magFilter
        set(value) { textureData.magFilter = checkNotLoaded(value, "MagFilter") }
    var colorConverter
        get() = textureData.colorConverter
        set(value) { textureData.colorConverter = checkNotLoaded(value, "ColorConverter") }

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
        if (assetIndex > NULL_BINDING_INDEX)
            return
        val textData = Engine.graphics.createTexture(this.textureData)
        assetIndex = textData.first
        width = textData.second
        height = textData.third
    }

    override fun deactivate() {
        forEachReference { ComponentSystem.deactivate(it) }
        super.deactivate()
    }

    override fun dispose() {
        forEachReference { ComponentSystem.dispose(it) }
        if (assetIndex <= NULL_BINDING_INDEX)
            return
        Engine.graphics.disposeTexture(assetIndex)
        assetIndex = NULL_BINDING_INDEX
        width = -1
        height = -1
    }

    override fun delete() {
        forEachReference { ComponentSystem.delete(it) }
        super.delete()
    }

    private fun forEachReference(action: (ComponentKey) -> Unit) {
        val itrA = Asset.iterator()
        while (itrA.hasNext()) {
            val it = itrA.next()
            if (it is Sprite && it.textureRef.targetKey == this.key) action(it.key)
            else if (it is SpriteSet && it.textureRef.targetKey == this.key) action(it.key)
            else if (it is Font && it.textureRef.targetKey == this.key) action(it.key)
        }
        val itrT = TileSet.iterator()
        while (itrT.hasNext()) {
            val it = itrT.next()
            if (it.textureRef.targetKey == this.key) action(it.key)
        }
    }

    companion object : ComponentSubTypeBuilder<Asset, Texture>(Asset,"Texture") {
        override fun create() = Texture()
    }
}