package com.inari.firefly.game.room.tiled_binding

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.Engine
import com.inari.firefly.core.LifecycleTaskType
import com.inari.firefly.core.StaticTask
import com.inari.firefly.core.api.*
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.Room.Companion.ATTR_OBJECT_LAYER
import com.inari.firefly.game.room.Room.Companion.ATTR_OBJECT_VIEW
import com.inari.firefly.game.room.TileMap
import com.inari.firefly.game.room.TileSet
import com.inari.firefly.game.world.Area
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.util.*
import com.inari.util.collection.Attributes
import com.inari.util.collection.Dictionary
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Vector4f

object TiledRoomLoadTask : StaticTask() {

    const val ATTR_RESOURCE = "tiledJsonResource"
    const val ATTR_ENCRYPTION = "encryptionKey"
    const val ATTR_TILE_SET_DIR_PATH = "tilesetDirPath"
    const val ATTR_VIEW_NAME = "viewName"
    const val ATTR_CREATE_LAYER = "createLayer"
    const val ATTR_DEFAULT_BLEND = "defaultBlend"
    const val ATTR_DEFAULT_TINT = "defaultTint"
    const val ATTR_ACTIVATION_TASKS = "activationTasks"
    const val ATTR_DEACTIVATION_TASKS = "deactivationTasks"
    const val ATTR_ACTIVATION_SCENE = "activationScene"
    const val ATTR_DEACTIVATION_SCENE = "deactivationScene"

    override fun apply(key : ComponentKey, attributes: Dictionary, callback: TaskCallback) {

        // get all needed attributes and check
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val viewName = attributes[ATTR_VIEW_NAME] ?:
            if (key != NO_COMPONENT_KEY)
                Area[key].viewRef.targetKey.name
            else
                throw IllegalArgumentException("Missing view ref")
        val enc = attributes[ATTR_ENCRYPTION]
        val tileSetDirPath = attributes[ATTR_TILE_SET_DIR_PATH] ?: EMPTY_STRING
        val activationTasks: MutableList<String> = attributes[ATTR_ACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()
        val deactivationTasks: MutableList<String> = attributes[ATTR_DEACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()

        // load tiled JSON resource
        val tileMapJson = Engine.resourceService.loadJSONResource(res, TiledMapJson::class, enc)
        val name = tileMapJson.mappedProperties[NAME_PROP]?.stringValue ?: throw IllegalArgumentException("Missing name")
        val createLayer = TiledRoomLoadTask.attributes[ATTR_CREATE_LAYER]?.toBoolean() ?: true


        // now create Room, TileMap and Objects from TiledMap input
        val roomKey = Room {
            this.name = name
            if (key != NO_COMPONENT_KEY && key.type == Area)
                areaRef(key)
            // set attributes and tasks
            this.attributes = tileMapJson.putProperties(Attributes())

            roomBounds(
                0f,
                0f,
                (tileMapJson.width * tileMapJson.tilewidth).toFloat(),
                (tileMapJson.height * tileMapJson.tileheight).toFloat())

            activationTasks.addAll(tileMapJson.mappedProperties[PROP_ROOM_ACTIVATION_TASKS]?.stringValue
                ?.split(VALUE_SEPARATOR) ?: emptyList())
            deactivationTasks.addAll(tileMapJson.mappedProperties[PROP_ROOM_DEACTIVATION_TASKS]?.stringValue
                ?.split(VALUE_SEPARATOR) ?: emptyList())
            val ait = activationTasks.iterator()
            while (ait.hasNext()) {
                withLifecycleTask {
                    this.order = 10
                    this.attributes = this@Room.attributes
                    lifecycleType = LifecycleTaskType.ON_ACTIVATION
                    task(ait.next())
                }
            }
            val dit = deactivationTasks.iterator()
            while (dit.hasNext()) {
                withLifecycleTask {
                    this.order = 10
                    this.attributes = this@Room.attributes
                    lifecycleType = LifecycleTaskType.ON_DEACTIVATION
                    task(dit.next())
                }
            }

            if (attributes.contains(this@TiledRoomLoadTask.ATTR_ACTIVATION_SCENE))
                activationScene(attributes[this@TiledRoomLoadTask.ATTR_ACTIVATION_SCENE]!!)
            if (attributes.contains(this@TiledRoomLoadTask.ATTR_DEACTIVATION_SCENE))
                deactivationScene(attributes[this@TiledRoomLoadTask.ATTR_DEACTIVATION_SCENE]!!)

        }

        val tileMapKey = TileMap {
            this.name = name
            viewRef(viewName)
            autoCreateLayer = createLayer
        }
        val tileMap = TileMap[tileMapKey]
        val room = Room[roomKey]
        room.withLifecycleComponent(tileMapKey)

        // load mapped tileset json files and create TiledTileSet components for it
        val tileSetReferences = tileMapJson.mappedProperties[PROP_TILE_SET_REFS]?.stringValue
            ?.split(LIST_VALUE_SEPARATOR)?.iterator()
            ?: throw IllegalArgumentException("Missing tileset_refs from TiledMap JSON file")

        val iter = tileMapJson.tilesets.iterator()
        while (iter.hasNext()) {
            val tileSetRefJson = iter.next()
            val tileSetRefName = tileSetReferences.next()
            if (TileSet.exists(tileSetRefName))
                continue

            val resPath = tileSetDirPath + tileSetRefJson.source.substringAfterLast('/')
            val tiledTileSetAttrs = Attributes() +
                    ( TiledTileSetLoadTask.ATTR_NAME to tileSetRefName ) +
                    ( TiledTileSetLoadTask.ATTR_RESOURCE to resPath) +
                    ( TiledTileSetLoadTask.ATTR_APPLY_ANIMATION_GROUPS to Room.ROOM_PAUSE_GROUP.aspectName)

            TiledTileSetLoadTask(attributes = tiledTileSetAttrs)

            val tileSet = TileSet[tileSetRefName]
            tileSet.mappingStartTileId = tileSetRefJson.firstgid
        }

        // load layers
        val iter2 = tileMapJson.layers.iterator()
        while (iter2.hasNext()) {
            val layerJson = iter2.next()
            if (layerJson.type == PROP_VALUE_TYPE_LAYER)
               loadTileLayer(tileMap, tileMapJson, layerJson)
            else if (layerJson.type == PROP_VALUE_TYPE_OBJECT)
                loadObjectLayer(viewName, room, layerJson)
        }

        callback(NO_COMPONENT_KEY, attributes, OperationResult.SUCCESS)
    }

    private fun loadTileLayer(
        tileMap: TileMap,
        tileMapJson: TiledMapJson,
        layerJson: TiledLayer) {


        val defaultBlend = BlendMode.valueOf(attributes[ATTR_DEFAULT_BLEND] ?: "NONE")
        val defaultTint = Vector4f()(attributes[ATTR_DEFAULT_TINT] ?: "1,1,1,1")

        // check tile-sets for layer are defined
        val layerTileSets = layerJson.mappedProperties[PROP_LAYER_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        tileMap.withTileLayer {
            position(layerJson.x + layerJson.offsetx, layerJson.y + layerJson.offsety)
            parallaxFactorX = layerJson.parallaxx - 1
            parallaxFactorY = layerJson.parallaxy - 1
            layerRef(layerJson.name)
            if (layerJson.tintcolor.isNotEmpty())
                tint(GeomUtils.colorOf(layerJson.tintcolor))
            else
                tint(defaultTint)
            if (layerJson.opacity < 1.0f)
                tint.a = layerJson.opacity
            blend = BlendMode.valueOf(layerJson.mappedProperties[BLEND_PROP]?.stringValue ?: defaultBlend.name)
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
                val iter =  layerTileSets.split(COMMA).iterator()
               while (iter.hasNext()) {
                   val tileSetName = iter.next()
                    withTileSetMapping {
                        tileSetRef(tileSetName)
                        codeOffset = TileSet[tileSetName].mappingStartTileId
                    }
                }
            }
        }
    }

    private fun loadObjectLayer(viewName: String, room: Room, layerJson: TiledLayer) {
        val iter = layerJson.objects?.iterator() ?: return
        while (iter.hasNext()) {
            val tiledObj = iter.next()
            val buildTask = tiledObj.mappedProperties[PROP_OBJECT_BUILD_TASK]?.stringValue
                ?: throw IllegalArgumentException("Missing object build task property")

            val attrs = tiledObj.toAttributes()
            attrs[ATTR_OBJECT_VIEW] = viewName
            attrs[ATTR_OBJECT_LAYER] = attrs[ATTR_OBJECT_LAYER]
                ?: throw IllegalArgumentException("Missing ATTR_OBJECT_LAYER")

            room.withLifecycleTask {
                this.order = 10
                this.attributes = attrs
                lifecycleType = LifecycleTaskType.ON_ACTIVATION
                task(buildTask)
            }
        }
    }
}