package com.inari.firefly.game.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.json.*
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.*
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import kotlin.jvm.JvmField

open class TileSet protected constructor(): Asset(TileSet) {

    @JvmField val textureRef = CReference(Texture)
    val textureIndex: Int
        get() = textureRef.targetKey.instanceIndex

    protected val tileTemplates = DynArray.of<TileTemplate>()
    val tiles: DynArrayRO<TileTemplate>
        get() = tileTemplates

    val withTile: (TileTemplate.() -> Unit) -> TileTemplate = { configure ->
        val tile = TileTemplate()
        tile.also(configure)
        tileTemplates.add(tile)
        tile
    }

    override fun setParentComponent(key: ComponentKey) {
        super.setParentComponent(key)
        if (key.type.aspectIndex == Asset.aspectIndex)
            textureRef(key)
        else
            textureRef.reset()
    }

    override fun load() {
        super.load()
        // load all sprites
        val textureIndex = resolveAssetIndex(textureRef.targetKey)
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

    override fun dispose() {
        for (i in 0 until tileTemplates.capacity) {
            val tileTemplate = tileTemplates[i] ?: continue
            Engine.graphics.disposeSprite(tileTemplate.spriteTemplate.spriteIndex)
            tileTemplate.spriteTemplate.spriteIndex = -1
            if (tileTemplate.animationData != null) {
                tileTemplate.animationData!!.sprites.values.forEach { aSpriteTemplate ->
                    Engine.graphics.disposeSprite(aSpriteTemplate.spriteIndex)
                    aSpriteTemplate.spriteIndex = -1
                }
            }
        }
        super.dispose()
    }

    companion object :  ComponentSubTypeSystem<Asset, TileSet>(Asset, "TileSet") {
        override fun create() = TileSet()
    }
}

/**
    TileType -> [name]_[material-type]_[tile-type]_[atlas-orientation]

    name = String
    material-type = String
    tile-type = [type]:[size]:[tile-direction]:[tile-orientation]
    atlas-orientation = [xpos]:[ypos]:[orientation]

    type = quad | slope | circle | rhombus | spike
    size = full [default] | half | quarter | quarterto
    tile-direction = none [default] | nw = north-west | n = north | ne = north-east | e = east | se = south-east | s = south | sw = south-west | w = west
    tile-orientation = none [default] | h = horizontal | v = vertical
    orientation = none [default] | h = horizontal flip | v = vertical flip | hv = horizontal and vertical flip | va = vertical and horizontal flip
 **/
class TiledJSONTileSet private constructor() : TileSet() {

    private var resource: () -> TileSetJson = { throw RuntimeException() }

    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { Engine.resourceService.loadJSONResource(value, TileSetJson::class, encryptionKey) }
        }
    fun withTileSetJson(tileSetJson: TileSetJson) {
        resource = { tileSetJson }
    }

    override fun load() {
        // first load all resources form Json file data
        loadFromJson()
        // then load the sprites from the templates as usual
        super.load()
    }

    private fun loadFromJson() {
        val tileSetJson = resource.invoke()

        // create texture asset for the tile set if not existing
        val textAssetName = "$TILE_SET_ASSET_NAME_PREFIX${this@TiledJSONTileSet.name}"
        if (Asset.exists(textAssetName)) {
            textureRef(textAssetName)
        }
        if (!textureRef.exists) {
            withChild(Texture) {
                name = textAssetName
                resourceName = tileSetJson.mappedProperties[PROP_NAME_ATLAS]?.stringValue ?: throw RuntimeException("Missing atlas texture resource")
            }
        }

        // create tile templates
        val tileDimType = TileUtils.getTileDimensionType(tileSetJson.tilewidth)
        tileSetJson.tiles.forEach { tileJson ->
            val tileParams = tileJson.type.split(UNDERLINE)
            val tileName = "${tileSetJson.name}_${tileJson.type}"
            val tileMaterial = tileParams[0]
            val tileTypeString = tileParams[1]
            val atlasPropsString = tileParams[2]
            val tileType = tileTypeString.split(COLON)
            val materialType = TileUtils.getTileMaterialType(tileMaterial)
            val contactFormType = TileUtils.getTileContactFormType(tileType[0])
            val size = TileUtils.getTileSizeType(tileType[1])
            val tileDirection = if (tileType.size > 2 )
                TileUtils.getTileDirection(tileType[2])
            else
                TileDirection.NONE

            val tileOrientation = if (tileType.size > 3)
                TileUtils.getTileOrientation(tileType[3])
            else
                TileOrientation.NONE

            val contactBitMask = TileUtils.createTileContactBitMask(
                contactFormType,
                size,
                tileOrientation,
                tileDirection,
                tileDimType
            )

            val atlasProps = atlasPropsString.split(COLON)
            val tileTintColor = TileUtils.getColorFromString(tileJson.mappedProperties[PROP_NAME_TINT]?.stringValue ?: EMPTY_STRING)
            val blend = BlendMode.valueOf(tileJson.mappedProperties[PROP_NAME_BLEND]?.stringValue ?: BlendMode.NONE.name)

            withTile {
                name = tileName
                blendMode = blend
                tintColor = tileTintColor
                material = materialType
                contactType = contactFormType
                contactMask = contactBitMask
                aspects + tileTypeString
                aspects + size
                aspects + tileDirection
                aspects + tileOrientation

                withSprite {
                    textureBounds(
                        atlasProps[0].toInt() * tileSetJson.tilewidth,
                        atlasProps[1].toInt() * tileSetJson.tileheight,
                        tileSetJson.tilewidth,
                        tileSetJson.tileheight)
                    hFlip = atlasProps.size > 2 && (atlasProps[2].contains(FLAG_NAME_HORIZONTAL))
                    vFlip = atlasProps.size > 2 && (atlasProps[2].contains(FLAG_NAME_VERTICAL))
                }

                if (tileJson.mappedProperties.contains(PROP_NAME_ANIMATION)) {
                    val frames = tileJson.mappedProperties[PROP_NAME_ANIMATION]!!.stringValue.split(COMMA)
                    withAnimation {
                        frames.forEachIndexed { index, fString ->
                            val timeSprite = fString.split(UNDERLINE)
                            val spriteProps = timeSprite[1].split(COLON)
                            withFrame {
                                interval = timeSprite[0].toLong()
                                spriteTemplate {
                                    name = tileName + "_$index"
                                    textureBounds(
                                        spriteProps[0].toInt() * tileSetJson.tilewidth,
                                        spriteProps[1].toInt() * tileSetJson.tileheight,
                                        tileSetJson.tilewidth,
                                        tileSetJson.tileheight)
                                    hFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_HORIZONTAL))
                                    vFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_VERTICAL))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun dispose() {
        super.dispose()
        tileTemplates.clear()
    }

    companion object :  ComponentSubTypeSystem<Asset, TiledJSONTileSet>(Asset, "TiledJSONTileSet") {
        override fun create() = TiledJSONTileSet()
    }
}