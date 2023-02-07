package com.inari.firefly.game.room.tiled_binding

import com.inari.firefly.core.Engine
import com.inari.firefly.core.LifecycleTaskType
import com.inari.firefly.core.Task
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.core.api.TaskCallback
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.TileMap
import com.inari.firefly.game.room.TileSet
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.util.COMMA
import com.inari.util.EMPTY_STRING
import com.inari.util.LIST_VALUE_SEPARATOR
import com.inari.util.collection.Attributes
import com.inari.util.collection.Dictionary
import com.inari.util.collection.EMPTY_DICTIONARY
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4f

object TiledRoomLoadTask : Task() {

    init {
        super.operation = TiledRoomLoadTask::apply
        Task.registerAsSingleton(this, true)
        Task.activate(this.name)
    }

    const val ATTR_NAME = "tileMapName"
    const val ATTR_RESOURCE = "tiledJsonResource"
    const val ATTR_ENCRYPTION = "encryptionKey"
    const val ATTR_TILE_SET_DIR_PATH = "tilesetDirPath"

    const val ATTR_VIEW_NAME = "viewName"
    const val ATTR_CREATE_LAYER = "createLayer"
    const val ATTR_DEFAULT_BLEND = "defaultBlend"
    const val ATTR_DEFAULT_TINT = "defaultTint"

    private fun apply(
        compIndex: ComponentIndex = NULL_COMPONENT_INDEX,
        attributes: Dictionary = EMPTY_DICTIONARY,
        callback: TaskCallback) {

        // get all needed attributes and check
        val name = attributes[ATTR_NAME] ?: throw IllegalArgumentException("Missing name")
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val viewName = attributes[ATTR_VIEW_NAME] ?: throw IllegalArgumentException("Missing view reference")
        val enc = attributes[ATTR_ENCRYPTION]
        val tileSetDirPath = attributes[ATTR_TILE_SET_DIR_PATH] ?: EMPTY_STRING

        val createLayer = attributes[ATTR_CREATE_LAYER]?.toBoolean() ?: true
        val defaultBlend = BlendMode.valueOf(attributes[ATTR_DEFAULT_BLEND] ?: "NONE")
        val defaultTint = Vector4f()(attributes[ATTR_DEFAULT_TINT] ?: "1,1,1,1")

        // load tiled JSON resource
        val tileMapJson = Engine.resourceService.loadJSONResource(res, TiledMapJson::class, enc)

        // now create Room, TileMap and Objects from TiledMap input
        //val tileSetOffsetMapping = mutableMapOf<String, Int>()
        val roomKey = Room {
            this.name = name
            // set attributes and tasks
            this.attributes = Attributes(tileMapJson.getPropertyStringMap())
            tileMapJson.mappedProperties["tasks"]?.apply {
                LifecycleTaskType.values().forEach {
                    this.classValue[it.name]?.apply {
                        this@Room.withTask(it, this as String)
                    }
                }
            }
            // create TileMap
            withTileMap {
                this.name = name
                viewRef(viewName)
            }
        }

        val tileMap = TileMap[name]

        // load mapped tileset json files and create TiledTileSet components for it
        val tileSetReferences = tileMapJson.mappedProperties[PROP_TILE_SET_REFS]?.stringValue
            ?.split(LIST_VALUE_SEPARATOR)?.iterator()
            ?: throw IllegalArgumentException("Missing tileset_refs from TiledMap JSON file")

        tileMapJson.tilesets.forEach { tileSetRefJson ->

            val tileSetRefName = tileSetReferences.next()
            val resPath = tileSetDirPath + tileSetRefJson.source.substringAfterLast('/')
            val tiledTileSetAttrs = Attributes() +
                    ( TiledTileSetLoadTask.ATTR_NAME to tileSetRefName ) +
                    (  TiledTileSetLoadTask.ATTR_RESOURCE to resPath)

            TiledTileSetLoadTask(attributes = tiledTileSetAttrs)

            val tileSet = TileSet[tileSetRefName]
            tileSet.mappingStartTileId = tileSetRefJson.firstgid
        }

        // load layers
        tileMapJson.layers.forEach { layerJson ->
            if (layerJson.type == PROP_VALUE_TYPE_LAYER)
               loadTileLayer(tileMap, tileMapJson, layerJson)
            else if (layerJson.type == PROP_VALUE_TYPE_OBJECT)
                loadObjectLayer(tileMap, tileMapJson, layerJson)
        }
    }

    private fun loadTileLayer(
        tileMap: TileMap,
        tileMapJson: TiledMapJson,
        layerJson: TiledLayer) {

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
                        codeOffset = TileSet[tileSetName].mappingStartTileId
                    }
                }
            }
        }
    }

    private fun loadObjectLayer(tileMap: TileMap, tileMapJson: TiledMapJson, layerJson: TiledLayer) {
        // TODO
    }

}