package com.inari.firefly.game.json

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.game.world.GameObjectComposite
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.util.Supplier
import com.inari.util.graphics.IColor
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class TiledJsonRoomAsset private constructor() : Asset() {

    private var tileMapId = NO_COMP_ID
    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()
    private var viewRef = -1
    private var resource: Supplier<TiledTileMap> = { throw RuntimeException() }

    @JvmField var view = ComponentRefResolver(View) { index -> viewRef = index }
    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, TiledTileMap::class, encryptionKey) }
        }
    fun withTiledTileMap(tileMapJson: TiledTileMap) {
        resource = { tileMapJson }
    }
    @JvmField var defaultBlend: BlendMode = BlendMode.NONE
    @JvmField var defaultTint: MutableColor = MutableColor(1f, 1f, 1f, 1f)

    override fun instanceId(index: Int): Int = tileMapId.instanceId

    override fun load() {
        if (tileMapId != NO_COMP_ID)
            return

        if (viewRef < 0 || !ViewSystem.views.contains(viewRef))
            throw RuntimeException("Missing view reference")

        val tileMapJson = resource.invoke()

        // create TiledTileSetAssets
        val tilesets = tileMapJson.mappedProperties["tilesets"]?.value?.split(",")
            ?: throw RuntimeException("Missing tilesets definition")

        tileSetAssetToCodeOffsetMapping.clear()
        tilesets.forEachIndexed() { index, tilsetString ->
            val tileSetProps = tilsetString.split(":")
            val tileSetName = tileSetProps[0]
            val tileSetAssetName = "${tileSetName}_tileSetAsset"
            val tilesetResource = tileSetProps[1]

            val tileSetAssetId = TiledJsonTileSetAsset.build {
                name = tileSetAssetName
                resourceName = tilesetResource
            }

            tileSetAssetToCodeOffsetMapping[tileSetName] = tileMapJson.tilesets[index].firstgid
        }

        // create TileMap
        tileMapId = TileMap.build {
            name = tileMapJson.mappedProperties["name"]?.value ?: super.name
            view(this@TiledJsonRoomAsset.viewRef)

            tileMapJson.layers.forEach { layerJson ->
                if (layerJson.type == "tilelayer")
                    this@TiledJsonRoomAsset.loadTileLayer(this, tileMapJson, layerJson)
                else if (layerJson.type == "objectgroup")
                    this@TiledJsonRoomAsset.loadObjectLayer(this, tileMapJson, layerJson)
            }
        }
    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {

        if (layerJson.mappedProperties.containsKey("addContactMap")) {
            val contactMap = SimpleContactMap.build {
                view(this@TiledJsonRoomAsset.viewRef)
                layer(layerJson.name)
            }
            tileMap.entityIds + contactMap
        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith("Composite")) {
                val typeName = tiledObject.type.split(":")

                val obj =  CompositeSystem.getCompositeBuilder<GameObjectComposite>(typeName[1]).buildAndGet {
                    name = tiledObject.name
                    view(this@TiledJsonRoomAsset.viewRef)
                    layer(layerJson.name)
                    tilesObjectProperties = tiledObject
                }
                obj.systemLoad()
                tileMap.entityIds + obj.componentId

            } else {
                //throw RuntimeException("Unknown Tiles Object Type: ${tiledObject.type}")
            }
        }
    }

    private fun loadTileLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {
        val layerTileSets = layerJson.mappedProperties["tilesets"]?.value
            ?: throw RuntimeException("Missing tilesets for layer")

        tileMap.withLayer {
            position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
            tileWidth = tileMapJson.tilewidth
            tileHeight = tileMapJson.tileheight
            mapWidth = layerJson.mappedProperties["width"]?.value?.toInt() ?: tileMapJson.width
            mapHeight = layerJson.mappedProperties["height"]?.value?.toInt() ?: tileMapJson.height
            parallaxFactorX = layerJson.parallaxx - 1
            parallaxFactorY = layerJson.parallaxy - 1
            layer(layerJson.name)
            mapCodes = layerJson.data!!
            spherical = tileMapJson.infinite
            if (layerJson.tintcolor.isNotEmpty())
                tint = IColor.ofMutable(layerJson.tintcolor)
            if (layerJson.opacity < 1.0f)
                tint(tint.r, tint.g, tint.b, layerJson.opacity)
            if ("blend" in layerJson.mappedProperties)
                blend = BlendMode.valueOf(layerJson.mappedProperties["blend"]?.value!!)
            if ("renderer" in layerJson.mappedProperties)
                renderer(layerJson.mappedProperties["renderer"]?.value!!)

            // define tile sets for this map layer
            val tileSetNames = layerTileSets.split(",")
            tileSetNames.forEach { tileSetName ->
                withTileSet {
                    tileSetAsset("${tileSetName}_tileSetAsset")
                    codeOffset = this@TiledJsonRoomAsset.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
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

    companion object : SystemComponentSubType<Asset, TiledJsonRoomAsset>(Asset, TiledJsonRoomAsset::class) {
        override fun createEmpty() = TiledJsonRoomAsset()
    }


}