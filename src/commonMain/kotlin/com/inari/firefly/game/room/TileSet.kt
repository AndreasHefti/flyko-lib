package com.inari.firefly.game.room

import com.inari.firefly.core.CReference
import com.inari.firefly.core.Component
import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import kotlin.jvm.JvmField

open class TileSet: Component(TileSet) {

    @JvmField var mappingStartTileId: Int = -1
    @JvmField val textureRef = CReference(Texture)
    val textureIndex: Int
        get() = textureRef.targetKey.componentIndex

    protected val tileTemplates = DynArray.of<TileTemplate>()
    val tiles: DynArrayRO<TileTemplate>
        get() = tileTemplates

    val withTile: (TileTemplate.() -> Unit) -> TileTemplate = { configure ->
        val tile = TileTemplate()
        tile.also(configure)
        tileTemplates.add(tile)
        tile
    }

    override fun activate() {
        super.load()

        // make sure texture for sprite set is defined and loaded
        if (!textureRef.exists)
            throw IllegalStateException("textureRef missing")
        val texture = Texture[textureRef.targetKey]
        if (!texture.loaded)
            Texture.load(textureRef.targetKey)

        // load all sprites on low level
        val textureIndex = texture.assetIndex
        tileTemplates.forEach { tileTemplate ->
            val spriteTemplate = tileTemplate.spriteTemplate
            spriteTemplate.textureIndex = textureIndex
            spriteTemplate.spriteIndex = Engine.graphics.createSprite(spriteTemplate)
            if (tileTemplate.animationData != null) {
                    tileTemplate.animationData!!.sprites.values.forEach { aSpriteTemplate ->
                        aSpriteTemplate.textureIndex = textureIndex
                        aSpriteTemplate.spriteIndex = Engine.graphics.createSprite(aSpriteTemplate)
                    }
            }
        }
    }

    override fun deactivate() {
        for (i in 0 until tileTemplates.capacity) {
            val tileTemplate = tileTemplates[i] ?: continue
            Engine.graphics.disposeSprite(tileTemplate.spriteTemplate.spriteIndex)
            tileTemplate.spriteTemplate.spriteIndex = NULL_COMPONENT_INDEX
            if (tileTemplate.animationData != null) {
                tileTemplate.animationData!!.sprites.values.forEach { aSpriteTemplate ->
                    Engine.graphics.disposeSprite(aSpriteTemplate.spriteIndex)
                    aSpriteTemplate.spriteIndex = NULL_COMPONENT_INDEX
                }
            }
        }
        super.dispose()
    }

    companion object : ComponentSystem<TileSet>("TileSet") {
        override fun allocateArray(size: Int): Array<TileSet?> = arrayOfNulls(size)
        override fun create() = TileSet()
    }
}


