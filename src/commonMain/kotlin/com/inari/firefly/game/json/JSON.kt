package com.inari.firefly.game.json

import com.inari.firefly.game.world.WorldOrientationType
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import kotlin.jvm.JvmField


const val COMPOSITE_OBJECT_NAME_PREFIX = "Composite"
const val ENTITY_COMPOSITE_OBJECT_NAME_PREFIX = "EComposite"
const val TILE_SET_ASSET_NAME_SUFFIX = "_tileSetAsset"
const val TILE_SET_ASSET_NAME_PREFIX = "tileSetAtlas_"

const val PROP_NAME_NAME = "name"
const val PROP_NAME_ATLAS = "atlas"
const val PROP_NAME_LOAD_TASK = "loadTask"
const val PROP_NAME_ACTIVATION_TASK = "activationTask"
const val PROP_NAME_DEACTIVATION_TASK = "deactivationTask"
const val PROP_NAME_DISPOSE_TASK = "disposeTask"
const val PROP_NAME_ADD_CONTACT_MAP = "addContactMap"
const val PROP_NAME_BLEND = "blend"
const val PROP_NAME_TINT = "tint"
const val PROP_NAME_RENDERER = "renderer"
const val PROP_NAME_ANIMATION = "animation"
const val FLAG_NAME_HORIZONTAL = "h"
const val FLAG_NAME_VERTICAL = "v"
const val PROP_NAME_WIDTH = "width"
const val PROP_NAME_HEIGHT = "height"
const val PROP_NAME_TILE_LAYER = "tilelayer"
const val PROP_NAME_OBJECT_LAYER = "objectgroup"
const val PROP_NAME_TILE_SETS = "tilesets"



/** example-world.json
 *  {
 *      "name": "world1",
 *      "orientationType": "COUNT",
 *      "orientation": "1",
 *      "onLoadTasks": "Task1|Task2|...",
 *      "onActivationTasks": "Task1|Task2|...",
 *      "onDeactivationTasks" :"Task1|Task2|...",
 *      "onDisposeTasks": "Task1|Task2|...",
 *      "attributes": {
 *          ...
 *      },
 *      "areasData": [{
 *          "name": "area1",
 *          "resource": "area1.json",
 *          "attributes": {
 *              ...
 *          }
 *      },
 *      {
 *          "name": "area2",
 *          "resource": "area2.json",
 *          "attributes": {
 *              ...
 *          }
 *      }]
 *  }
 */
class WorldJson(
    @JvmField val name: String,
    @JvmField val orientationType: WorldOrientationType = WorldOrientationType.COUNT,
    @JvmField val orientation: String = "0",
    @JvmField val onLoadTasks: String = NO_NAME,
    @JvmField val onActivationTasks: String = NO_NAME,
    @JvmField val onDeactivationTasks: String = NO_NAME,
    @JvmField val onDisposeTasks: String = NO_NAME,
    @JvmField val attributes: Map<String, String> = emptyMap(),
    @JvmField val areasData: Collection<AreaMetaJson>
)
class AreaMetaJson(
    @JvmField val name: String,
    @JvmField val resource: String,
    @JvmField val attributes: Map<String, String> = emptyMap()
)

/** example-area.json
 *  {
 *      "name": "area1",
 *      "orientationType": "COUNT",
 *      "orientation": "1",
 *      "onLoadTasks": "Task1|Task2|...",
 *      "onActivationTasks": "Task1|Task2|...",
 *      "onDeactivationTasks" :"Task1|Task2|...",
 *      "onDisposeTasks": "Task1|Task2|...",
 *      "attributes": {
 *          ...
 *      },
 *      "roomsData": [{
 *          "name": "room1",
 *          "tiledMapResource": "room1Map.json",
 *          "orientationType": "TILES",
 *          "orientation": "0,0,40,20",
 *          "onLoadTasks": "Task1|Task2|...",
 *          "onActivationTasks": "Task1|Task2|...",
 *          "onDeactivationTasks" :"Task1|Task2|...",
 *          "onDisposeTasks": "Task1|Task2|...",
 *          "attributes": {
 *              ...
 *          }
 *      },
 *      {
 *          "name": "room2",
 *          "tiledMapResource": "room1Map.json",
 *          "orientationType": "tiles|pixels",
 *          "orientationType": "PIXELS",
 *          "orientation": "0,0,40,20",
 *          "onLoadTasks": "Task1|Task2|...",
 *          "onActivationTasks": "Task1|Task2|...",
 *          "onDeactivationTasks" :"Task1|Task2|...",
 *          "onDisposeTasks": "Task1|Task2|...",
 *          "pauseTask": "RoomPauseTask",
 *          "resumeTask": "RoomResumeTask",
 *          "activationScene": "Scene1",
 *          "deactivationScene": "Scene1",
 *          "attributes": {
 *              ...
 *          }
 *      }]
 *  }
 */
class AreaJson(
    @JvmField val name: String,
    @JvmField val orientationType: WorldOrientationType = WorldOrientationType.COUNT,
    @JvmField val orientation: String = "0",
    @JvmField val onLoadTasks: String = NO_NAME,
    @JvmField val onActivationTasks: String = NO_NAME,
    @JvmField val onDeactivationTasks: String = NO_NAME,
    @JvmField val onDisposeTasks: String = NO_NAME,
    @JvmField val attributes: Map<String, String> = emptyMap(),
    @JvmField val roomsData: Collection<RoomMetaJson>
)
class RoomMetaJson(
    @JvmField val name: String,
    @JvmField val roomOrientationType: WorldOrientationType = WorldOrientationType.PIXELS,
    @JvmField val roomOrientation: String = "0,0,0,0",
    @JvmField val areaOrientationType: WorldOrientationType = WorldOrientationType.SECTION,
    @JvmField val areaOrientation: String = "0,0,0,0",
    @JvmField val onLoadTasks: String = NO_NAME,
    @JvmField val onActivationTasks: String = NO_NAME,
    @JvmField val onDeactivationTasks: String = NO_NAME,
    @JvmField val onDisposeTasks: String = NO_NAME,
    @JvmField val pauseTask: String = NO_NAME,
    @JvmField val resumeTask: String = NO_NAME,
    @JvmField val activationScene: String = NO_NAME,
    @JvmField val deactivationScene: String = NO_NAME,
    @JvmField val attributes: Map<String, String> = emptyMap()
)



class TiledTileMap(
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val tilewidth: Int,
    @JvmField val tileheight: Int,
    @JvmField val infinite: Boolean = false,
    @JvmField val orientation: String,
    @JvmField val layers: Array<TiledLayer> = emptyArray(),
    @JvmField val tilesets: Array<TilesetCodeOffsets> = emptyArray(),
    @JvmField val properties: Array<PropertyJson> = emptyArray()
) {
    val mappedProperties: Map<String, PropertyJson>
        get() = properties.associateBy(PropertyJson::name)
}

class TiledLayer(
    @JvmField val name: String,
    @JvmField val type: String,               // "tilelayer" or "objectgroup"
    @JvmField val x: Int,                     // position in pixel
    @JvmField val y: Int,                     // position in pixel
    @JvmField val width: Int = 0,             // width in tiles
    @JvmField val height: Int = 0,            // height in tiles
    @JvmField val offsetx: Float = 0f,        // offset x-axis in pixel
    @JvmField val offsety: Float = 0f,        // offset y-axis in pixel
    @JvmField val parallaxx: Float = 1f,      // parallax scroll factor
    @JvmField val parallaxy: Float = 1f,      // parallax scroll factor
    @JvmField val opacity: Float = 1f,
    @JvmField val tintcolor: String = "",          // #rrggbbaa
    @JvmField val visible: Boolean,
    @JvmField val data: IntArray?,
    @JvmField val objects: Array<TiledObject>?,
    @JvmField val properties: Array<PropertyJson> = emptyArray()
) {
    val mappedProperties: Map<String, PropertyJson>
        get() = properties.associateBy(PropertyJson::name)
}

class TiledObject(
    @JvmField val id: Int = -1,
    @JvmField val name: String = NO_NAME,
    @JvmField val type: String = NO_NAME,
    @JvmField val x: Float = ZERO_FLOAT,
    @JvmField val y: Float = ZERO_FLOAT,
    @JvmField val width: Float = ZERO_FLOAT,
    @JvmField val height: Float = ZERO_FLOAT,
    @JvmField val rotation: Float = ZERO_FLOAT,
    @JvmField val visible: Boolean = false,
    @JvmField val gid: Int = -1,
    @JvmField val point: Boolean = false,
    @JvmField val properties: Array<PropertyJson> = emptyArray()
){
    val mappedProperties: Map<String, PropertyJson>
        get() = properties.associateBy(PropertyJson::name)
}

class PropertyJson(
    @JvmField val name: String,
    @JvmField val type: String,
    @JvmField val value: Any
) {
    val stringValue: String
        get() = value.toString()
    val intValue: Int
        get() = value as Int
}

class TilesetCodeOffsets(
    @JvmField val firstgid: Int,
)

class TileSetJson(
    @JvmField val name: String,
    @JvmField val tilewidth: Int,
    @JvmField val tileheight: Int,
    @JvmField val columns: Int,
    @JvmField val tilecount: Int,
    @JvmField val properties: Array<PropertyJson> = emptyArray(),
    @JvmField val tiles: Array<TileJson> = emptyArray()
) {
    val mappedProperties: Map<String, PropertyJson>
        get() = properties.associateBy(PropertyJson::name)
}

class TileJson(
    @JvmField val id: Int,
    @JvmField val type: String,
    @JvmField val properties: Array<PropertyJson> = emptyArray()
) {
    val mappedProperties: Map<String, PropertyJson>
        get() = properties.associateBy(PropertyJson::name)
}

