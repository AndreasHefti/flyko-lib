package com.inari.firefly.game.tile

import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ArrayAccessor
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.graphics.TextureAsset
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class TileSet private constructor() : SystemComponent(TileSet::class.simpleName!!) {

    @JvmField internal var textureAssetRef: Int = -1
    @JvmField internal val tileTemplates = DynArray.of<TileTemplate>()
    @JvmField internal var spriteSetAssetId = NO_COMP_ID

    val texture = ComponentRefResolver(TextureAsset) { index -> textureAssetRef = index }

    val withTile: (TileTemplate.() -> Unit) -> TileTemplate = { configure ->
        val tile = TileTemplate()
        tile.also(configure)
        tileTemplates.add(tile)
        tile
    }
    val tiles = ArrayAccessor(tileTemplates)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<TileSet>(TileSet::class) {
        override fun createEmpty() = TileSet()
    }
}