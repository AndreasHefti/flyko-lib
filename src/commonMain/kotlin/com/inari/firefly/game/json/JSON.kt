package com.inari.firefly.game.json

import com.inari.firefly.NO_NAME
import com.inari.firefly.game.world.WorldOrientationType

/** example-world.json
 *  {
 *      "name": "world1",
 *      "orientationType": "COUNT",
 *      "orientation": "1",
 *      "onActivationTasks": = "Task1,Task2,...",
 *      "attributess": {
 *          ...
 *      },
 *      "areasData": [{
 *          "name": "area1",
 *          "resource": "area1.json",
 *          "attributess": {
 *              ...
 *          }
 *      },
 *      {
 *          "name": "area2",
 *          "resource": "area2.json",
 *          "attributess": {
 *              ...
 *          }
 *      }]
 *  }
 */
data class WorldJson(
    val name: String,
    val orientationType: WorldOrientationType = WorldOrientationType.COUNT,
    val orientation: String = "0",
    val onActivationTasks: String? = null,
    val attributes: Map<String, String> = emptyMap(),
    val areasData: Collection<AreaMetaJson>
)
data class AreaMetaJson(
    val name: String,
    val resource: String,
    val attributes: Map<String, String> = emptyMap()
)

/** example-area.json
 *  {
 *      "name": "area1",
 *      "orientationType": "COUNT",
 *      "orientation": "1",
 *      "onActivationTasks": = "Task1,Task2,...",
 *      "attributess": {
 *          ...
 *      },
 *      "roomsData": [{
 *          "name": "room1",
 *          "tiledMapResource": "room1Map.json",
 *          "orientationType": "TILES",
 *          "orientation": "0,0,40,20",
 *          "onActivationTasks": = "Task1,Task2,...",
 *          "attributess": {
 *              ...
 *          }
 *      },
 *      {
 *          "name": "room2",
 *          "tiledMapResource": "room1Map.json",
 *          "orientationType": "tiles|pixels",
 *          "orientationType": "PIXELS",
 *          "orientation": "0,0,40,20",
 *          "onActivationTasks": = "Task1,Task2,...",
 *          "attributess": {
 *              ...
 *          }
 *      }]
 *  }
 */
data class AreaJson(
    val name: String,
    val orientationType: WorldOrientationType = WorldOrientationType.COUNT,
    val orientation: String = "0",
    val onActivationTasks: String? = null,
    val attributes: Map<String, String> = emptyMap(),
    val roomsData: Collection<RoomMetaJson>
)
data class RoomMetaJson(
    val name: String,
    val tiledMapResource: String? = null,
    val orientationType: WorldOrientationType = WorldOrientationType.TILES,
    val orientation: String = "0,0,0,0",
    val onActivationTasks: String? = null,
    val attributes: Map<String, String> = emptyMap()
)



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
    val type: String,               // "tilelayer" or "objectgroup"
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
    val data: IntArray?,
    val objects: Array<TiledObject>?,
    val properties: Array<PropertyJson> = emptyArray()
) {
    val mappedProperties: Map<String, PropertyJson> = properties.associateBy(PropertyJson::name)
}

data class TiledObject(
    val id: Int = -1,
    val name: String = NO_NAME,
    val type: String = NO_NAME,
    val x: Float = 0.0f,
    val y: Float = 0.0f,
    val width: Float = 0.0f,
    val height: Float = 0.0f,
    val rotation: Float = 0.0f,
    val visible: Boolean = false,
    val gid: Int = -1,
    val point: Boolean = false,
    val properties: Array<PropertyJson> = emptyArray()
)

data class PropertyJson(
    val name: String,
    val type: String,
    val value: String
)

data class TilesetCodeOffsets(
    val firstgid: Int,
)

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

