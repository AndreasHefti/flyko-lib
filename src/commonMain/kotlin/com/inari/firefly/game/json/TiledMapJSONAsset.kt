package com.inari.firefly.game.json

import com.inari.firefly.*
import com.inari.firefly.asset.Asset
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.EComposite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.game.world.objects.RoomObjectComposite
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.physics.contact.SimpleContactMap
import com.inari.util.Supplier
import com.inari.util.geom.GeomUtils.colorOf
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class TiledMapJSONAsset private constructor() : Asset() {

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
    @JvmField var defaultTint: Vector4f = Vector4f(1f, 1f, 1f, 1f)

    override fun instanceId(index: Int): Int = tileMapId.instanceId

    override fun load() {
        if (tileMapId != NO_COMP_ID)
            return

        if (viewRef < 0 || !ViewSystem.views.contains(viewRef))
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
            if (FFContext.exists(Asset, tileSetAssetName))
                FFContext.activate(Asset, tileSetAssetName)
            else
                TiledTileSetJSONAsset.buildAndActivate {
                    name = tileSetAssetName
                    resourceName = tilesetResource
                }

            tileSetAssetToCodeOffsetMapping[tileSetName] = tileMapJson.tilesets[index].firstgid
        }

        // create TileMap
        tileMapId = TileMap.build {
            name = tileMapJson.mappedProperties[PROP_NAME_NAME]?.stringValue ?: super.name
            view(this@TiledMapJSONAsset.viewRef)

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
                view(this@TiledMapJSONAsset.viewRef)
                layer(layerJson.name)
            }
            tileMap.compositeIds + contactMap
        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX)) {
                val typeName = tiledObject.type.split(COLON)

                val composite = CompositeSystem.getCompositeBuilder<RoomObjectComposite>(typeName[1]).buildAndGet {
                    name = tiledObject.name
                    view(this@TiledMapJSONAsset.viewRef)
                    layer(layerJson.name)
                    layerJson.properties.forEach { setAttribute(it.name, it.stringValue) }
                }
                composite.systemLoad()
                tileMap.compositeIds + composite.componentId

            } else if (tiledObject.type.startsWith(ENTITY_COMPOSITE_OBJECT_NAME_PREFIX)) {

                Entity.build {
                    name = tiledObject.name
                    withComponent(ETransform) {
                        view(this@TiledMapJSONAsset.viewRef)
                        layer(layerJson.name)
                        position(tiledObject.x, tiledObject.y)
                        rotation = tiledObject.rotation
                    }
                    withComponent(EComposite) {
                        layerJson.properties.forEach {
                            setAttribute(it.name, it.stringValue)
                        }
                        tiledObject.mappedProperties[PROP_NAME_LOAD_TASK]?.also {
                            withLoadTask(it.stringValue)
                        }
                        tiledObject.mappedProperties[PROP_NAME_ACTIVATION_TASK]?.also {
                            withActivationTask(it.stringValue)
                        }
                        tiledObject.mappedProperties[PROP_NAME_DEACTIVATION_TASK]?.also {
                            withDeactivationTask(it.stringValue)
                        }
                        tiledObject.mappedProperties[PROP_NAME_DISPOSE_TASK]?.also {
                            withDisposeTask(it.stringValue)
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

        tileMap.withLayer {
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
                renderer(layerJson.mappedProperties[PROP_NAME_RENDERER]?.stringValue!!)

            // define tile sets for this map layer
            val tileSetNames = layerTileSets.split(COMMA)
            tileSetNames.forEach { tileSetName ->
                withTileSet {
                    tileSetAsset("${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
                    codeOffset = this@TiledMapJSONAsset.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
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
            FFContext.deactivate(Asset, "${tileSetName}${TILE_SET_ASSET_NAME_SUFFIX}")
        }
        tileSetAssetToCodeOffsetMapping.clear()
        tileMapId = NO_COMP_ID
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, TiledMapJSONAsset>(Asset, TiledMapJSONAsset::class) {
        override fun createEmpty() = TiledMapJSONAsset()
    }

}