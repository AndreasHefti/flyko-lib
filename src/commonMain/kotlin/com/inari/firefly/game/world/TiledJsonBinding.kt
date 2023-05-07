package com.inari.firefly.game.world

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.Engine
import com.inari.firefly.core.StaticTask
import com.inari.firefly.core.api.ActionResult
import com.inari.firefly.core.api.TaskCallback
import com.inari.firefly.game.*
import com.inari.util.*
import com.inari.util.collection.Attributes
import com.inari.util.collection.AttributesRO
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

@Suppress("UNCHECKED_CAST")
class TiledPropertyJson(
    @JvmField val name: String,
    @JvmField val type: String,
    @JvmField val value: Any
) {
    val stringValue: String
        get() = value.toString()
    val intValue: Int
        get() = value as Int
    val classValue: Map<String, Any>
        get() = value as Map<String, Any>
}

abstract class WithMappedProperties(
    val props: Array<TiledPropertyJson> = emptyArray(),
) {
    val mappedProperties: Map<String, TiledPropertyJson> =
        props.filter {
            if (it.type == TILED_PROP_STRING_TYPE)
                it.value != EMPTY_STRING
            else
                true
        }.associateBy(TiledPropertyJson::name)

    fun getSubPropAsString(name: String, subName: String): String? =
        mappedProperties[name]?.classValue?.get(subName)?.toString()
    fun getSubPropAsBoolean(name: String, subName: String): Boolean? =
        mappedProperties[name]?.classValue?.get(subName)?.toString()?.toBoolean()
    fun getSubPropAsInt(name: String, subName: String): Int? =
        mappedProperties[name]?.classValue?.get(subName)?.toString()?.toFloat()?.toInt()

    fun putProperties(attributes: Attributes): Attributes {
        val iter = mappedProperties.entries.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            if (it.value.type == TILED_PROP_STRING_TYPE)
                attributes[it.value.name] = it.value.stringValue
            else {
                try {
                    fillProps(it.value.classValue, attributes)
                } catch (e: Exception) {
                    println("Failed to parse tiled property: $it")
                }
            }
        }
        return attributes
    }

    @Suppress("UNCHECKED_CAST")
    private fun fillProps(props: Map<String, Any>, attributes: Attributes) {
        val iter = props.entries.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            if (it.value is String || it.value is Number)
                attributes[it.key] = it.value.toString()
            else try {
                fillProps(it.value as Map<String, Any>, attributes)
            } catch (e: Exception) {
                println("Failed to parse tiled property: $it")
            }
        }
    }
}

class TiledTileSetJson(
    @JvmField val name: String,
    @JvmField val tilewidth: Int,
    @JvmField val tileheight: Int,
    @JvmField val columns: Int,
    @JvmField val tilecount: Int,
    @JvmField val properties: Array<TiledPropertyJson> = emptyArray(),
    @JvmField val tiles: Array<TiledTileJson> = emptyArray()
) : WithMappedProperties(properties)

class TiledTileJson(
    @JvmField val id: Int,
    @JvmField val type: String,
    @JvmField val properties: Array<TiledPropertyJson> = emptyArray()
) : WithMappedProperties(properties)

class TiledTileSetRefJson(
    @JvmField val firstgid: Int,
    @JvmField val source: String
)

class TiledMapJson(
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val tilewidth: Int,
    @JvmField val tileheight: Int,
    @JvmField val infinite: Boolean = false,
    @JvmField val orientation: String,
    @JvmField val layers: Array<TiledLayer> = emptyArray(),
    @JvmField val tilesets: Array<TiledTileSetRefJson> = emptyArray(),
    @JvmField val properties: Array<TiledPropertyJson> = emptyArray()
) : WithMappedProperties(properties)

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
    @JvmField val properties: Array<TiledPropertyJson> = emptyArray()
) : WithMappedProperties(properties)

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
    @JvmField val properties: Array<TiledPropertyJson> = emptyArray()
) : WithMappedProperties(properties) {

    fun toAttributes(): Attributes {
        val attributes: Attributes = Attributes() +
                ( ATTR_OBJECT_ID to id.toString() ) +
                ( ATTR_OBJECT_TYPE to type ) +
                ( ATTR_OBJECT_NAME to name ) +
                ( ATTR_OBJECT_BOUNDS to Vector4f(x, y, width, height).toJsonString() ) +
                ( ATTR_OBJECT_ROTATION to rotation.toString() ) +
                ( ATTR_OBJECT_VISIBLE to visible.toString() )

        putProperties(attributes)
        return attributes
    }
}

object  TiledTileSetLoadTask : StaticTask() {

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {

        // get all needed attributes and check
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]
        // load tiled JSON resource
        val tileSetJson = Engine.resourceService.loadJSONResource(res, TiledTileSetJson::class, enc)

        JsonTileSetLoadTask.load(
            attributes,
            TiledTileSetConversionTask.convert(tileSetJson))

        callback(NO_COMPONENT_KEY, attributes, ActionResult.SUCCESS)
    }
}

object TiledRoomLoadTask : StaticTask() {

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {

        // get all needed attributes and check
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]
        // load tiled JSON resource
        val tileMapJson = Engine.resourceService.loadJSONResource(res, TiledMapJson::class, enc)

        loadTileSets(attributes, tileMapJson)
        val converted = TiledRoomConversionTask.convert(attributes, tileMapJson)
        JsonRoomLoadTask.loadRoom(key, attributes, converted)

        callback(NO_COMPONENT_KEY, attributes, ActionResult.SUCCESS)
    }

    private fun loadTileSets(attributes: AttributesRO, tileMapJson :  TiledMapJson) {
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

            val tileSetDirPath = attributes[ATTR_TILE_SET_DIR_PATH] ?: EMPTY_STRING
            val resPath = tileSetDirPath + tileSetRefJson.source.substringAfterLast('/')
            val tiledTileSetAttrs = Attributes() +
                    ( ATTR_TILE_SET_NAME to tileSetRefName ) +
                    ( ATTR_RESOURCE to resPath) +
                    ( ATTR_APPLY_ANIMATION_GROUPS to Room.ROOM_PAUSE_GROUP.aspectName)

            TiledTileSetLoadTask(attributes = tiledTileSetAttrs)

            val tileSet = TileSet[tileSetRefName]
            tileSet.mappingStartTileId = tileSetRefJson.firstgid
        }
    }
}
