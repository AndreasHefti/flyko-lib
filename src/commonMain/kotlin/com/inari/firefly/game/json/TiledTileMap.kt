package com.inari.firefly.game.json

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.util.COLON
import com.inari.util.COMMA
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class TiledTileMap private constructor() : TileMap() {

    @JvmField var resourceSupplier: () -> TiledTileMapData = { throw RuntimeException() }
    @JvmField var encryptionKey: String? = null
    @JvmField var defaultBlend = BlendMode.NONE
    @JvmField var defaultTint = Vector4f(1f, 1f, 1f, 1f)

    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()

    override fun load() {
        super.load()
        if (!viewRef.exists)
            throw RuntimeException("Missing view reference")

        val tileMapJson = resourceSupplier()

        // create TiledTileSetAssets
        val tileSets = tileMapJson.mappedProperties[PROP_NAME_TILE_SETS]?.stringValue?.split(COMMA)
            ?: throw RuntimeException("Missing tile sets definition")

        tileSetAssetToCodeOffsetMapping.clear()
        tileSets.forEachIndexed() { index, tilesetString ->
            val tileSetProps = tilesetString.split(COLON)
            val tileSetName = tileSetProps[0]
            val tileSetAssetName = "${tileSetName}$TILE_SET_ASSET_NAME_SUFFIX"
            val tilesetResource = tileSetProps[1]

            // activate existing tileset or create new one and activate
            if (!TiledTileSet.exists(tileSetAssetName))
                TiledTileSet {
                    autoActivation = true
                    name = tileSetAssetName
                    resourceName = tilesetResource
                }
            TiledTileSet.load(tileSetAssetName)
            tileSetAssetToCodeOffsetMapping[tileSetName] = tileMapJson.tilesets[index].firstgid
        }

        tileMapJson.layers.forEach { layerJson ->
            if (layerJson.type == PROP_NAME_TILE_LAYER)
                this@TiledTileMap.loadTileLayer(this, tileMapJson, layerJson)
            else if (layerJson.type == PROP_NAME_OBJECT_LAYER)
                this@TiledTileMap.loadObjectLayer(this, tileMapJson, layerJson)
        }
    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledTileMapData, layerJson: TiledLayer) {

        if (layerJson.mappedProperties.containsKey(PROP_NAME_ADD_CONTACT_MAP)) {
            val contactMap = SimpleContactMap.build {
                viewRef(this@TiledTileMap.viewRef)
                layerRef(layerJson.name)
            }
            tileMap.withChild(contactMap)
        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX)) {
                val typeName = tiledObject.type.split(COLON)

                tileMap.withChild(ComponentSystem.getComponentBuilder<Composite>(typeName[1])) {
                    name = tiledObject.name
                    viewRef(tileMap.viewRef)
                    layerRef(layerJson.name)
                    position(tiledObject.x, tiledObject.y)
                    setAttribute("rotation", tiledObject.rotation.toString())
                    layerJson.properties.forEach { setAttribute(it.name, it.stringValue) }
                }

            } else {
                throw RuntimeException("Unknown Tiles Object Type: ${tiledObject.type}")
            }
        }
    }

    private fun loadTileLayer(tileMap: TileMap, tileMapJson: TiledTileMapData, layerJson: TiledLayer) {
        val layerTileSets = layerJson.mappedProperties[PROP_NAME_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        tileMap.withTileLayer {
            position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
            tileWidth = tileMapJson.tilewidth
            tileHeight = tileMapJson.tileheight
            mapWidth = layerJson.mappedProperties[PROP_NAME_WIDTH]?.intValue ?: tileMapJson.width
            mapHeight = layerJson.mappedProperties[PROP_NAME_HEIGHT]?.intValue ?: tileMapJson.height
            parallaxFactorX = layerJson.parallaxx - 1
            parallaxFactorY = layerJson.parallaxy - 1
            layer(layerJson.name)
            mapCodes = layerJson.data!!
            spherical = tileMapJson.infinite
            if (layerJson.tintcolor.isNotEmpty())
                tint = GeomUtils.colorOf(layerJson.tintcolor)
            if (layerJson.opacity < 1.0f)
                tint.a = layerJson.opacity
            if (PROP_NAME_BLEND in layerJson.mappedProperties)
                blend = BlendMode.valueOf(layerJson.mappedProperties[PROP_NAME_BLEND]?.stringValue!!)
            if (PROP_NAME_RENDERER in layerJson.mappedProperties)
                renderer = ViewSystemRenderer.byName(layerJson.mappedProperties[PROP_NAME_RENDERER]?.stringValue!!)

            // define tile sets for this map layer
            val tileSetNames = layerTileSets.split(COMMA)
            tileSetNames.forEach { tileSetName ->
                withTileSetMapping {
                    tileSetRef("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
                    codeOffset = this@TiledTileMap.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
                }
            }
        }
    }

    override fun dispose() {
        tileSetAssetToCodeOffsetMapping.keys.forEach{ tileSetName ->
            TiledTileSet.deactivate("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
        }
        tileSetAssetToCodeOffsetMapping.clear()
        val tileMapJson = resourceSupplier()
        tileMapJson.layers.forEach { layerJson ->
            layerJson.objects?.forEach { tiledObject ->
                if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX))
                    Composite.delete(tiledObject.name)
            }
        }

        super.dispose()
    }

    companion object : ComponentSubTypeBuilder<TileMap, TiledTileMap>(TileMap,"TiledTileMapAsset") {
        override fun create() = TiledTileMap()
    }
}