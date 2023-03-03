package com.inari.firefly.game.room.tiled_binding

import com.inari.firefly.game.room.Room
import com.inari.util.EMPTY_STRING
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import com.inari.util.collection.Attributes
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

const val TILED_PROP_STRING_TYPE = "string"
const val COMPOSITE_OBJECT_NAME_PREFIX = "Composite"
const val ATLAS_ASSET_NAME_PREFIX = "atlas_"
const val COLLISION_ASSET_NAME_PREFIX = "collisionMask_"

const val NAME_PROP = "name"
const val ATLAS_PROP = "atlas_file_path"
const val COLLISION_PROP = "collision_bitmask_file_path"

const val MATERIAL_PROP = "material"
const val MATERIAL_TYPE_PROP = "material_type"
const val CONTACT_TYPE_PROP = "contact_type"
const val CONTACT_FULL_PROP = "contact_full_rect"
const val CONTACT_X_PROP = "contact_mask_x"
const val CONTACT_Y_PROP = "contact_mask_y"
const val ATLAS_POS_PROP = "atlas_position"
const val ATLAS_X_PROP = "x"
const val ATLAS_Y_PROP = "y"
const val ATLAS_FLIP_PROP = "atlas_sprite_flip"
const val BLEND_PROP = "blend_mode"
const val TINT_PROP = "tint_color"
const val ASPECT_PROP = "tint"
const val ANIMATION_PROP = "animation"
const val RENDERER_PROP = "renderer"

const val HORIZONTAL_VALUE = "horizontal"
const val VERTICAL_VALUE = "vertical"
const val FLAG_NAME_HORIZONTAL = "h"
const val FLAG_NAME_VERTICAL = "v"

const val PROP_ROOM_TASKS = "tasks"
const val PROP_VALUE_TYPE_LAYER = "tilelayer"
const val PROP_VALUE_TYPE_OBJECT = "objectgroup"
const val PROP_LAYER_TILE_SETS = "layer_tilesets"
const val PROP_TILE_SET_REFS = "tileset_refs"
const val PROP_OBJECT_TASKS = "tasks"
const val PROP_OBJECT_BUILD_TASK = "build_task"

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
    private val props: Array<TiledPropertyJson> = emptyArray(),
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
                ( Room.ATTR_OBJECT_ID to id.toString() ) +
                ( Room.ATTR_OBJECT_TYPE to type ) +
                ( Room.ATTR_OBJECT_NAME to name ) +
                ( Room.ATTR_OBJECT_BOUNDS to Vector4f(x, y, width, height).toJsonString() ) +
                ( Room.ATTR_OBJECT_ROTATION to rotation.toString() ) +
                ( Room.ATTR_OBJECT_VISIBLE to visible.toString() )

        putProperties(attributes)
        return attributes
    }
}
