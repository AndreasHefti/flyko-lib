package com.inari.firefly.game.tile.tiled_binding

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.tile.TileMap
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.util.*
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class TiledTileMap private constructor() : TileMap() {

    private var resourceSupplier: () -> TiledTileMapJson = { throw RuntimeException() }
    private val tileSetAssetToCodeOffsetMapping = mutableMapOf<String, Int>()

    @JvmField var tilesetAssetDirectory: String = EMPTY_STRING
    @JvmField var encryptionKey: String? = null
    @JvmField var defaultBlend = BlendMode.NONE
    @JvmField var defaultTint = Vector4f(1f, 1f, 1f, 1f)

    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resourceSupplier = { Engine.resourceService.loadJSONResource(value, TiledTileMapJson::class, encryptionKey) }
        }

    fun withTileMapJson(tileMapJson: TiledTileMapJson) {
        resourceSupplier = { tileMapJson }
    }

    override fun load() {
        super.load()

        if (!viewRef.exists)
            throw RuntimeException("Missing view reference")

        val tileMapJson = resourceSupplier()

        // set attributes and tasks
        super.attributes = Attributes(tileMapJson.getPropertyStringMap())
        tileMapJson.mappedProperties["tasks"]?.apply {
            LifecycleTaskType.values().forEach {
                this.classValue[it.name]?.apply {
                    super.withTask(it, this as String)
                }
            }
        }

        // load mapped tileset json files and create TiledTileSet components for it
        tileMapJson.tilesets.forEach { tileSetRefJson ->
            val resPath =  tilesetAssetDirectory + tileSetRefJson.source.substringAfterLast('/')
            val tiledTileSetJson = Engine.resourceService.loadJSONResource(
                resPath,
                TiledTileSetJson::class, encryptionKey)

            if (!TiledTileSet.exists(tiledTileSetJson.name)) {
                TiledTileSet {
                    name = tiledTileSetJson.name
                    withTileSetJson(tiledTileSetJson)
                }
                tileSetAssetToCodeOffsetMapping[tiledTileSetJson.name] = tileSetRefJson.firstgid
            }
        }

        // load layers
        tileMapJson.layers.forEach { layerJson ->
            if (layerJson.type == PROP_VALUE_TYPE_LAYER)
                this@TiledTileMap.loadTileLayer(this, tileMapJson, layerJson)
            else if (layerJson.type == PROP_VALUE_TYPE_OBJECT)
                this@TiledTileMap.loadObjectLayer(this, tileMapJson, layerJson)
        }

    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledTileMapJson, layerJson: TiledLayer) {

//        if (layerJson.mappedProperties.containsKey(PROP_NAME_ADD_CONTACT_MAP)) {
//            val contactMap = SimpleContactMap.build {
//                viewRef(this@TiledTileMap.viewRef)
//                layerRef(layerJson.name)
//            }
//            tileMap.withChild(contactMap)
//        }

        layerJson.objects!!.forEach { tiledObject ->

            if (tiledObject.type.startsWith(COMPOSITE_OBJECT_NAME_PREFIX)) {
                val typeName = tiledObject.type.split(COLON)

//                tileMap.withChild(ComponentSystem.getComponentBuilder<Composite>(typeName[1])) {
//                    name = tiledObject.name
//                    viewRef(tileMap.viewRef)
//                    layerRef(layerJson.name)
//                    position(tiledObject.x, tiledObject.y)
//                    setAttribute("rotation", tiledObject.rotation.toString())
//                    layerJson.properties.forEach { setAttribute(it.name, it.stringValue) }
//                }

            } else {
                throw RuntimeException("Unknown Tiles Object Type: ${tiledObject.type}")
            }
        }
    }

    private fun loadTileLayer(tileMap: TileMap, tileMapJson: TiledTileMapJson, layerJson: TiledLayer) {
        // check tile-sets for layer are defined
        val layerTileSets = layerJson.mappedProperties[PROP_LAYER_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        tileMap.withTileLayer {
            position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
            parallaxFactorX = layerJson.parallaxx - 1
            parallaxFactorY = layerJson.parallaxy - 1
            layerRef(layerJson.name)
            if (layerJson.tintcolor.isNotEmpty())
                tint = GeomUtils.colorOf(layerJson.tintcolor)
            if (layerJson.opacity < 1.0f)
                tint.a = layerJson.opacity
            if (BLEND_PROP in layerJson.mappedProperties)
                blend = BlendMode.valueOf(layerJson.mappedProperties[BLEND_PROP]?.stringValue!!)


            withTileGridData {
                tileWidth = tileMapJson.tilewidth
                tileHeight = tileMapJson.tileheight
                mapWidth = tileMapJson.width
                mapHeight = tileMapJson.height
                mapCodes = layerJson.data!!
                spherical = tileMapJson.infinite
                if (RENDERER_PROP in layerJson.mappedProperties)
                    renderer = ViewSystemRenderer.byName(layerJson.mappedProperties[RENDERER_PROP]?.stringValue!!)

                // define tile sets for this map
                layerTileSets.split(COMMA).forEach { tileSetName ->
                    withTileSetMapping {
                        tileSetRef(tileSetName)
                        codeOffset = this@TiledTileMap.tileSetAssetToCodeOffsetMapping[tileSetName] ?: 0
                    }
                }
            }
        }
    }

    override fun dispose() {
        tileSetAssetToCodeOffsetMapping.keys.forEach{ tileSetName ->
            TiledTileSet.deactivate(tileSetName)
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