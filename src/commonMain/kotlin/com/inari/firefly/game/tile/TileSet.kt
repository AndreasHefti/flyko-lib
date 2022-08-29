package com.inari.firefly.game.tile

import com.inari.firefly.core.CReference
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.graphics.sprite.SpriteSet
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import kotlin.jvm.JvmField

class TileSet private constructor(): Component(TileSet) {

    @JvmField val textureRef = CReference(Texture)
    val textureIndex: Int
        get() = textureRef.targetKey.instanceIndex

    @JvmField internal val tileTemplates = DynArray.of<TileTemplate>()
    val tiles: DynArrayRO<TileTemplate> = tileTemplates
    internal var spriteSetKey: ComponentKey = NO_COMPONENT_KEY

    val withTile: (TileTemplate.() -> Unit) -> TileTemplate = { configure ->
        val tile = TileTemplate()
        tile.also(configure)
        tileTemplates.add(tile)
        tile
    }

    override fun load() {
        // create sprite set asset for the tile set
        val tileSet = this@TileSet
        spriteSetKey = SpriteSet.build {
            name = tileSet.name
            textureRef( tileSet.textureIndex)
            tileSet.tileTemplates.forEach { tileTemplate ->
                withSpriteInfo(tileTemplate.protoSprite)
                if (tileTemplate.animationData != null) {
                    tileTemplate.animationData!!.sprites.values.forEach { info -> withSpriteInfo(info) }
                }
            }
        }
    }

    override fun dispose() {
        SpriteSet.delete(spriteSetKey)
        spriteSetKey = NO_COMPONENT_KEY
    }

    override val componentType = Companion
    companion object : ComponentSystem<TileSet>("TileSet") {
        override fun allocateArray(size: Int): Array<TileSet?> = arrayOfNulls(size)
        override fun create() = TileSet()
    }
}