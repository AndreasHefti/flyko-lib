package com.inari.firefly.game.json

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.composite.EComposite
import com.inari.firefly.game.composite.WordViewComposite
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.util.COLON
import com.inari.util.COMMA
import com.inari.util.NO_NAME
import com.inari.util.geom.GeomUtils.colorOf
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class TiledMapJSONAsset private constructor() : Asset() {

    private var tileMapKey = NO_COMPONENT_KEY
    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()
    private val viewRef: Int
        get() = view.targetKey.instanceIndex
    private var resource: () -> TiledTileMap = { throw RuntimeException() }

    @JvmField var view = CReference(View)
    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { Engine.resourceService.loadJSONResource(value, TiledTileMap::class, encryptionKey) }
        }
    fun withTiledTileMap(tileMapJson: TiledTileMap) {
        resource = { tileMapJson }
    }
    @JvmField var defaultBlend = BlendMode.NONE
    @JvmField var defaultTint = Vector4f(1f, 1f, 1f, 1f)

    override fun assetIndex(index: Int): Int = tileMapKey.instanceIndex

    override fun load() {

        if (viewRef < 0 || !View.exists(viewRef))
            throw RuntimeException("Missing view reference")

        val tileMapJson = resource.invoke()

        // create TiledTileSetAssets
        val tileSets = tileMapJson.mappedProperties[PROP_NAME_TILE_SETS]?.stringValue?.split(COMMA)
            ?: throw RuntimeException("Missing tile sets definition")

        tileSetAssetToCodeOffsetMapping.clear()
        tileSets.forEachIndexed() { index, tilesetString ->
            val tileSetProps = tilesetString.split(COLON)
            val tileSetName = tileSetProps[0]
            val tileSetAssetName = "${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}"
            val tilesetResource = tileSetProps[1]

            // activate existing tileset asset or create new one and activate
            if (Asset.exists(tileSetAssetName))
                Asset.activate(tileSetAssetName)
            else
                TiledTileSetJSONAsset.buildActive {
                    name = tileSetAssetName
                    resourceName = tilesetResource
                }

            tileSetAssetToCodeOffsetMapping[tileSetName] = tileMapJson.tilesets[index].firstgid
        }

        // create TileMap
        tileMapKey = TileMap.build {
            name = tileMapJson.mappedProperties[PROP_NAME_NAME]?.stringValue ?: super.name
            viewRef(this@TiledMapJSONAsset.viewRef)

            tileMapJson.layers.forEach { layerJson ->
                if (layerJson.type == PROP_NAME_TILE_LAYER)
                    this@TiledMapJSONAsset.loadTileLayer(this, tileMapJson, layerJson)
                else if (layerJson.type == PROP_NAME_OBJECT_LAYER)
                    this@TiledMapJSONAsset.loadObjectLayer(this, tileMapJson, layerJson)
            }
        }
    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {

        if (layerJson.mappedProperties.containsKey(PROP_NAME_ADD_CONTACT_MAP)) {
            val contactMap = SimpleContactMap.build {
                viewRef(this@TiledMapJSONAsset.viewRef)
                layerRef(layerJson.name)
            }
            tileMap.withChild(contactMap)
            //tileMap.compositeIds + contactMap
        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX)) {
                val typeName = tiledObject.type.split(COLON)

                tileMap.withChild(ComponentSystem.getComponentBuilder<WordViewComposite>(typeName[1])) {
                    name = tiledObject.name
                    view(this@TiledMapJSONAsset.viewRef)
                    layer(layerJson.name)
                    position(tiledObject.x, tiledObject.y)
                    setAttribute("rotation", tiledObject.rotation.toString())
                    layerJson.properties.forEach { setAttribute(it.name, it.stringValue) }
                }
//
//
//                Composite.load(composite.index)
//                tileMap.compositeIds + composite.componentId

            } else if (tiledObject.type.startsWith(ENTITY_COMPOSITE_OBJECT_NAME_PREFIX)) {

                Entity.build {
                    tiledObject.mappedProperties[PROP_NAME_LOAD_TASK]?.also {
                        onLoadTask = it.stringValue
                    }
                    tiledObject.mappedProperties[PROP_NAME_ACTIVATION_TASK]?.also {
                        onActivationTask = it.stringValue
                    }
                    tiledObject.mappedProperties[PROP_NAME_DEACTIVATION_TASK]?.also {
                        onDeactivationTask = it.stringValue
                    }
                    tiledObject.mappedProperties[PROP_NAME_DISPOSE_TASK]?.also {
                        onDisposeTask = it.stringValue
                    }

                    name = tiledObject.name
                    withComponent(ETransform) {
                        viewRef(this@TiledMapJSONAsset.viewRef)
                        layerRef(layerJson.name)
                        position(tiledObject.x, tiledObject.y)
                        rotation = tiledObject.rotation
                    }
                    withComponent(EComposite) {
                        layerJson.properties.forEach {
                            setAttribute(it.name, it.stringValue)
                        }
                    }
                }

            } else {
                //throw RuntimeException("Unknown Tiles Object Type: ${tiledObject.type}")
            }
        }
    }

    private fun loadTileLayer(tileMap: TileMap, tileMapJson: TiledTileMap, layerJson: TiledLayer) {
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
                tint = colorOf(layerJson.tintcolor)
            if (layerJson.opacity < 1.0f)
                tint.a = layerJson.opacity
            if (PROP_NAME_BLEND in layerJson.mappedProperties)
                blend = BlendMode.valueOf(layerJson.mappedProperties[PROP_NAME_BLEND]?.stringValue!!)
            if (PROP_NAME_RENDERER in layerJson.mappedProperties)
                renderer = ViewSystemRenderer.byName(layerJson.mappedProperties[PROP_NAME_RENDERER]?.stringValue!!)

            // define tile sets for this map layer
            val tileSetNames = layerTileSets.split(COMMA)
            tileSetNames.forEach { tileSetName ->
                withTileSet {
                    tileSetRef("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
                    codeOffset = this@TiledMapJSONAsset.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
                }
            }
        }
    }

    override fun dispose() {
        // dispose the tile map
        TileMap.delete(tileMapKey)
        // dispose all tile set assets
        tileSetAssetToCodeOffsetMapping.keys.forEach{ tileSetName ->
            Asset.deactivate("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
        }
        tileSetAssetToCodeOffsetMapping.clear()
        tileMapKey = NO_COMPONENT_KEY
    }

    companion object :  ComponentSubTypeSystem<Asset, TiledMapJSONAsset>(Asset, "TiledMapJSONAsset") {
        override fun create() = TiledMapJSONAsset()
    }

}