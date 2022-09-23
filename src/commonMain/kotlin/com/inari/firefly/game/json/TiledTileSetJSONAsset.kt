package com.inari.firefly.game.json

import com.inari.firefly.*
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.tile.*
import com.inari.firefly.graphics.TextureAsset
import com.inari.util.Supplier
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
class TiledTileSetJSONAsset private constructor() : Asset() {

    private var tileSetId = NO_COMP_ID
    private var textureAssetId = NO_COMP_ID
    private var resource: Supplier<TileSetJson> = { throw RuntimeException() }

    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, TileSetJson::class, encryptionKey) }
        }
    fun withTileSetJson(tileSetJson: TileSetJson) {
        resource = { tileSetJson }
    }

    override fun instanceId(index: Int): Int = tileSetId.instanceId

    override fun load() {
        if (tileSetId != NO_COMP_ID)
            return

        val tileSetJson = resource.invoke()

        // create texture asset for the tile set if not exists and load it
        val atlasResource = tileSetJson.mappedProperties[PROP_NAME_ATLAS]?.stringValue ?: throw RuntimeException("Missing atlas texture resource")
        AssetSystem.assets.forEach loop@ {  asset ->
            if (asset is TextureAsset && atlasResource == asset.resourceName) {
                textureAssetId = asset.componentId
                return@loop
            }
        }
        if (textureAssetId == NO_COMP_ID) {
            textureAssetId = TextureAsset.build {
                name = "${TILE_SET_ASSET_NAME_PREFIX}${this@TiledTileSetJSONAsset.name}"
                resourceName = atlasResource
            }
        }
        FFContext.activate(TextureAsset, textureAssetId)

        val tileDimType = TileUtils.getTileDimensionType(tileSetJson.tilewidth)
        // create tile set
        tileSetId = TileSet.build {
            name = tileSetJson.name
            texture(this@TiledTileSetJSONAsset.textureAssetId)

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

                val contactBitMask = TileMapSystem.createTileContactBitMask(
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
                                    protoSprite {
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
    }

    override fun unload() {
        if (tileSetId == NO_COMP_ID)
            return

        FFContext.delete(tileSetId)
        tileSetId = NO_COMP_ID
        textureAssetId = NO_COMP_ID
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, TiledTileSetJSONAsset>(Asset, TiledTileSetJSONAsset::class) {
        override fun createEmpty() = TiledTileSetJSONAsset()
    }

}