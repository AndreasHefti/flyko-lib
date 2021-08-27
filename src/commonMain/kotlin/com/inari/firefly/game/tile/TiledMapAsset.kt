package com.inari.firefly.game.tile

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.util.Supplier
import com.inari.util.graphics.IColor
import com.inari.util.graphics.MutableColor

class TiledMapAsset private constructor() : Asset() {

    private var tileMapId = NO_COMP_ID
    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()
    private var viewRef = -1
    private var resource: Supplier<TiledTileMap> = { throw RuntimeException() }

    var view = ComponentRefResolver(View) { index -> viewRef = index }
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, TiledTileMap::class) }
        }
    fun withTileSetJson(tileMapJson: TiledTileMap) {
        resource = { tileMapJson }
    }
    var defaultBlend: BlendMode = BlendMode.NONE
    var defaultTint: MutableColor = MutableColor(1f, 1f, 1f, 1f)

    override fun instanceId(index: Int): Int = tileMapId.instanceId

    override fun load() {
        if (tileMapId != NO_COMP_ID)
            return

        if (viewRef < 0 || !ViewSystem.views.contains(viewRef))
            throw RuntimeException("Missing view reference")

        val tileMapJson = resource.invoke()

        // create TiledTileSetAssets
        val tilesets = tileMapJson.mappedProperties["tilesets"]?.value?.split(",")
        if (tilesets == null)
            throw RuntimeException("Missing tilesets definition")

        tileSetAssetToCodeOffsetMapping.clear()
        tilesets.forEachIndexed() { index, tilsetString ->
            val tileSetProps = tilsetString.split(":")
            val tileSetName = tileSetProps[0]
            val tileSetAssetName = "${tileSetName}_tileSetAsset"
            val tilesetResource = tileSetProps[1]

            val tileSetAssetId = TiledTileSetAsset.build {
                name = tileSetAssetName
                resourceName = tilesetResource
            }

            tileSetAssetToCodeOffsetMapping.put(tileSetName, tileMapJson.tilesets[index].firstgid)
        }

        // create TileMap
        tileMapId = TileMap.build {
            name = tileMapJson.mappedProperties["name"]?.value ?: super.name
            view(this@TiledMapAsset.viewRef)

            tileMapJson.layers.forEach { layerJson ->
                val layerTileSets = layerJson.mappedProperties["tilesets"]?.value
                if (layerTileSets == null)
                    throw RuntimeException("Missing tilesets for layer")

                withLayer {
                    position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
                    tileWidth = tileMapJson.tilewidth
                    tileHeight = tileMapJson.tileheight
                    mapWidth = layerJson.mappedProperties["width"]?.value?.toInt() ?: tileMapJson.width
                    mapHeight = layerJson.mappedProperties["height"]?.value?.toInt() ?: tileMapJson.height
                    parallaxFactorX = layerJson.parallaxx
                    parallaxFactorY = layerJson.parallaxy
                    layer(layerJson.name)
                    mapCodes = layerJson.data
                    spherical = tileMapJson.infinite
                    if (!layerJson.tintcolor.isEmpty())
                        tint = IColor.Companion.ofMutable(layerJson.tintcolor)
                    if ("blend" in layerJson.mappedProperties)
                        blend = BlendMode.valueOf(layerJson.mappedProperties["blend"]?.value!!)
                    if ("renderer" in layerJson.mappedProperties)
                        renderer(layerJson.mappedProperties["renderer"]?.value!!)

                    // define tile sets for this map layer
                    val tileSetNames = layerTileSets.split(",")
                    tileSetNames.forEach { tileSetName ->
                        withTileSet {
                            tileSetAsset("${tileSetName}_tileSetAsset")
                            codeOffset = this@TiledMapAsset.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
                        }
                    }
                }
            }
        }
    }

    override fun unload() {
        if (tileMapId == NO_COMP_ID)
            return

        // dispose the tile map
        FFContext.delete(TileMap, tileMapId)
        // dispose all tile set assets
        tileSetAssetToCodeOffsetMapping.keys.forEach{ tileSetName ->
            FFContext.delete(Asset, "${tileSetName}_tileSetAsset")
        }
        tileSetAssetToCodeOffsetMapping.clear()
        tileMapId = NO_COMP_ID
    }

    companion object : SystemComponentSubType<Asset, TiledMapAsset>(Asset, TiledMapAsset::class) {
        override fun createEmpty() = TiledMapAsset()
    }

    data class TiledTileMap(
        val width: Int,
        val height: Int,
        val tilewidth: Int,
        val tileheight: Int,
        val infinite: Boolean = false,
        val orientation: String,
        val layers: Array<TiledLayer> = emptyArray(),
        val tilesets: Array<TilesetCodeOffsets> = emptyArray(),
        val properties: Array<PropertyJson> = emptyArray()
    ) {
        val mappedProperties: Map<String, PropertyJson> = properties.associateBy(PropertyJson::name)
    }

    data class TiledLayer(
        val name: String,
        val x: Int,                     // position in pixel
        val y: Int,                     // position in pixel
        val width: Int,                 // width in tiles
        val height: Int,                // height in tiles
        val offsetx: Float = 0f,        // offset x-axis in pixel
        val offsety: Float = 0f,        // offset y-axis in pixel
        val parallaxx: Float = 1f,      // parallax scroll factor
        val parallaxy: Float = 1f,      // parallax scroll factor
        val opacity: Float = 1f,
        val tintcolor: String = "",          // #rrggbbaa
        val visible: Boolean,
        val data: IntArray,
        val properties: Array<PropertyJson> = emptyArray()
    ) {
        val mappedProperties: Map<String, PropertyJson> = properties.associateBy(PropertyJson::name)
    }

    data class PropertyJson(
        val name: String,
        val type: String,
        val value: String
    )

    data class TilesetCodeOffsets(
        val firstgid: Int,
    )
}