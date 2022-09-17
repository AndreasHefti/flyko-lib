package com.inari.firefly.game.json

import com.inari.firefly.core.Asset
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.tile.TileDirection
import com.inari.firefly.game.tile.TileOrientation
import com.inari.firefly.game.tile.TileSet
import com.inari.firefly.game.tile.TileUtils
import com.inari.firefly.graphics.sprite.Texture
import com.inari.util.*
import kotlin.jvm.JvmField

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
class TiledTileSetAsset private constructor() : Asset(TiledTileSetAsset) {

    //private var tileSetId = NO_COMPONENT_KEY
    private var textureAssetId = NO_COMPONENT_KEY
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
        val tileSetJson = resource.invoke()

        // create texture asset for the tile set if not existing
        val textAssetName = "$TILE_SET_ASSET_NAME_PREFIX${this@TiledTileSetAsset.name}"
        if (Texture.exists(textAssetName)) {
            textureAssetId = Texture.getKey(textAssetName)
        }
        if (textureAssetId == NO_COMPONENT_KEY) {
            textureAssetId = withChild(Texture) {
                name = textAssetName
                resourceName = tileSetJson.mappedProperties[PROP_NAME_ATLAS]?.stringValue
                    ?: throw RuntimeException("Missing atlas texture resource")
            }
        }
        Texture.activate(textureAssetId)

        // create tile templates
        val tileDimType = TileUtils.getTileDimensionType(tileSetJson.tilewidth)
        // create tile set
        assetIndex = TileSet {
            name = this@TiledTileSetAsset.name
            textureRef(this@TiledTileSetAsset.textureAssetId)
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
                val tileDirection = if (tileType.size > 2)
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
                val tileTintColor =
                    TileUtils.getColorFromString(tileJson.mappedProperties[PROP_NAME_TINT]?.stringValue ?: EMPTY_STRING)
                val blend =
                    BlendMode.valueOf(tileJson.mappedProperties[PROP_NAME_BLEND]?.stringValue ?: BlendMode.NONE.name)

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
                            tileSetJson.tileheight
                        )
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
                                            tileSetJson.tileheight
                                        )
                                        hFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_HORIZONTAL))
                                        vFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_VERTICAL))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.instanceIndex
    }

    override fun dispose() {
        if (assetIndex < 0) {
            return
        }

        TileSet.delete(assetIndex)
        assetIndex = -1
        textureAssetId = NO_COMPONENT_KEY
    }

    companion object : ComponentSubTypeBuilder<Asset, TiledTileSetAsset>(Asset,"TiledTileSetAsset") {
        override fun create() = TiledTileSetAsset()
    }
}