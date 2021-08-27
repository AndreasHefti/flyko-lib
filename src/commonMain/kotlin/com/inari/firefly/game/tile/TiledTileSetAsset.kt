package com.inari.firefly.game.tile

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.TextureAsset
import com.inari.util.Supplier

class TiledTileSetAsset private constructor() : Asset() {

    private var tileSetId = NO_COMP_ID
    private var textureAssetId = NO_COMP_ID
    private var resource: Supplier<TileSetJson> = { throw RuntimeException() }

    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, TileSetJson::class) }
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
        val atlasResource = tileSetJson.mappedProperties["atlas"]?.value ?: throw RuntimeException("Missing atlas texture resource")
        AssetSystem.assets.forEach loop@ {  asset ->
            if (asset is TextureAsset && atlasResource == asset.resourceName) {
                textureAssetId = asset.componentId
                return@loop
            }
        }
        if (textureAssetId == NO_COMP_ID) {
            textureAssetId = TextureAsset.build {
                name = "texture_${this@TiledTileSetAsset.name}"
                resourceName = atlasResource
            }
        }
        FFContext.activate(TextureAsset, textureAssetId)

        val tileDimType = TileUtils.getTileDimensionType(tileSetJson.tilewidth)
        // create tile set
        tileSetId = TileSet.build {
            name = tileSetJson.name
            texture(this@TiledTileSetAsset.textureAssetId)

            tileSetJson.tiles.forEach { tileJson ->
//                TileType -> [name]_[material-type]_[tile-type]_[atlas-orientation]
//
//                name = String
//                material-type = String
//                tile-type = [type]:[size]:[tile-direction]:[tile-orientation]
//                atlas-orientation = [xpos]:[ypos]:[orientation]
//
//                type = quad | slope | circle | rhombus | spike
//                size = full [default] | half | quarter | quarterto
//                tile-direction = none [default] | nw = north-west | n = north | ne = north-east | e = east | se = south-east | s = south | sw = south-west | w = west
//                tile-orientation = none [default] | h = horizontal | v = vertical
//                orientation = none [default] | h = horizontal flip | v = vertical flip | hv = horizontal and vertical flip | va = vertical and horizontal flip

                val tile_params = tileJson.type.split("_")
                val tileName = "${tileSetJson.name}_${tileJson.type}"
                val tileMaterial = tile_params[0]
                val tileTypeString = tile_params[1]
                val atlasPropsString = tile_params[2]
                val tile_type = tileTypeString.split(":")
                val materialType = TileUtils.getTileMaterialType(tileMaterial)
                val contactFormType = TileUtils.getTileContactFormType(tile_type[0])
                val size = TileUtils.getTileSizeType(tile_type[1])
                val tileDirection = if (tile_type.size > 2 )
                    TileUtils.getTileDirection(tile_type[2])
                    else
                        TileDirection.NONE

                val tileOrientation = if (tile_type.size > 3)
                    TileUtils.getTileOrientation(tile_type[3])
                    else
                        TileOrientation.NONE

                val contactBitMask = TileMapSystem.createTileContactBitMask(
                    contactFormType,
                    size,
                    tileOrientation,
                    tileDirection,
                    tileDimType
                )

                val atlasProps = atlasPropsString.split(":")
                val tileTintColor = TileUtils.getColorFromString(tileJson.mappedProperties["blend"]?.value ?: "")

                withTile {
                    name = tileName
                    blendMode = try {
                        BlendMode.valueOf(tileJson.mappedProperties["blend"]!!.value)
                    } catch (e: Exception) {
                        null
                    }
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
                        hFlip = atlasProps.size > 2 && (atlasProps[2].contains("h"))
                        vFlip = atlasProps.size > 2 && (atlasProps[2].contains("v"))
                    }

                    if (tileJson.mappedProperties.contains("animation")) {
                        val frames = tileJson.mappedProperties["animation"]!!.value.split(",")
                        withAnimation {
                            frames.forEachIndexed { index, fString ->
                                val timeSprite = fString.split("_")
                                val spriteProps = timeSprite[1].split(":")
                                withFrame {
                                    interval = timeSprite[0].toLong()
                                    protoSprite {
                                        name = tileName + "_$index"
                                        textureBounds(
                                            spriteProps[0].toInt() * tileSetJson.tilewidth,
                                            spriteProps[1].toInt() * tileSetJson.tileheight,
                                            tileSetJson.tilewidth,
                                            tileSetJson.tileheight)
                                        hFlip = spriteProps.size > 2 && (spriteProps[2].contains("h"))
                                        vFlip = spriteProps.size > 2 && (spriteProps[2].contains("v"))
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

    companion object : SystemComponentSubType<Asset, TiledTileSetAsset>(Asset, TiledTileSetAsset::class) {
        override fun createEmpty() = TiledTileSetAsset()
    }

    data class TileSetJson(
        val name: String,
        val tilewidth: Int,
        val tileheight: Int,
        val columns: Int,
        val tilecount: Int,
        val properties: Array<PropertyJson> = emptyArray(),
        val tiles: Array<TileJson> = emptyArray()
    ) {
        val mappedProperties: Map<String, PropertyJson> = properties.associateBy(PropertyJson::name)
    }

    data class TileJson(
        val id: Int,
        val type: String,
        val properties: Array<PropertyJson> = emptyArray()
    ) {
        val mappedProperties: Map<String, PropertyJson> = properties.associateBy(PropertyJson::name)
    }

    data class PropertyJson(
        val name: String,
        val type: String,
        val value: String
    )
}