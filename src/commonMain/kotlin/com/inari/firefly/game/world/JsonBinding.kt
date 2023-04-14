package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.core.api.TaskCallback
import com.inari.firefly.graphics.sprite.AccessibleTexture
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ViewSystemRenderer
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_MATERIAL
import com.inari.util.*
import com.inari.util.collection.Attributes
import com.inari.util.collection.AttributesRO
import com.inari.util.geom.*
import kotlin.jvm.JvmField

// Task attribute names
const val ATTR_RESOURCE = "jsonFileResource"
const val ATTR_ENCRYPTION = "encryptionKey"
const val ATTR_TILE_SET_DIR_PATH = "tilesetDirPath"
const val ATTR_VIEW_NAME = "viewName"
const val ATTR_DEFAULT_BLEND = "defaultBlend"
const val ATTR_DEFAULT_TINT = "defaultTint"
const val ATTR_ACTIVATION_TASKS = "activationTasks"
const val ATTR_DEACTIVATION_TASKS = "deactivationTasks"
const val ATTR_ACTIVATION_SCENE = "activationScene"
const val ATTR_DEACTIVATION_SCENE = "deactivationScene"
const val ATTR_TILESET_OVERRIDE_PREFIX = "tsOverride-"

const val ATTR_TILE_SET_NAME = "tileSetName"
const val ATTR_MAPPING_START_TILE_ID = "mappingStartTileId"
const val ATTR_APPLY_TILE_SET_GROUPS = "applyTileSetGroups"
const val ATTR_APPLY_ANIMATION_GROUPS = "applyAnimationGroups"
const val ATTR_TARGET_FILE = "targetFile"

const val ATTR_OBJECT_ID = "id"
const val ATTR_OBJECT_TYPE = "type"
const val ATTR_OBJECT_NAME = "name"
const val ATTR_OBJECT_BOUNDS = "bounds"
const val ATTR_OBJECT_ROTATION = "rotation"
const val ATTR_OBJECT_SCALE = "scale"
const val ATTR_OBJECT_VISIBLE = "visible"
const val ATTR_OBJECT_GROUPS = "groups"
const val ATTR_OBJECT_VIEW = "viewName"
const val ATTR_OBJECT_LAYER = "layerName"
const val ATTR_OBJECT_ORDER = "order"

const val ATTR_TRANSITION_BUILD_TASK = "RoomTransitionBuildTask"
const val ATTR_TRANSITION_CONDITION = "condition"
const val ATTR_TRANSITION_TARGET = "target"
const val ATTR_TRANSITION_ID = "transitionId"
const val ATTR_TRANSITION_ORIENTATION = "orientation"

// JSON names
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
const val SCALE_X = "scale_x"
const val SCALE_Y = "scale_y"

const val HORIZONTAL_VALUE = "horizontal"
const val VERTICAL_VALUE = "vertical"
const val FLAG_NAME_HORIZONTAL = "h"
const val FLAG_NAME_VERTICAL = "v"

const val PROP_ROOM_ACTIVATION_TASKS = "activation_tasks"
const val PROP_ROOM_DEACTIVATION_TASKS = "deactivation_tasks"
const val PROP_VALUE_TYPE_LAYER = "tilelayer"
const val PROP_VALUE_TYPE_OBJECT = "objectgroup"
const val PROP_LAYER_TILE_SETS = "layer_tilesets"
const val PROP_TILE_SET_REFS = "tileset_refs"
const val PROP_OBJECT_BUILD_TASK = "build_task"

abstract class JsonFile {
    abstract val type: String                                       // [FileType[tileset||room||area||world]]
}

class AreaJson(
    override val type: String = "area",                             // [type]
    @JvmField val name: String,                                     // [area-name]
    @JvmField val view: String = NO_NAME,                           // [camera-name]
    @JvmField val camera: String = NO_NAME,                         // [camera-name]
    @JvmField val loadScene: String = NO_NAME,                      // [scene-name]
    @JvmField val disposeScene: String = NO_NAME,                   // [scene-name]
    @JvmField val roomActivationScene: String = NO_NAME,            // [scene-name]
    @JvmField val roomDeactivationScene: String = NO_NAME,          // [scene-name]
    @JvmField val props: String?,                                   // [attr1=v1|attr2=v2|...]
    @JvmField val loadInParallel: Boolean = false,                  // [true|false]
    @JvmField val stopLoadSceneWhenLoadFinished: Boolean = true,    // [true|false]
    @JvmField val activateAfterLoadScene: Boolean = true,           // [true|false]
    @JvmField val rooms: Array<RoomFileReference>
) : JsonFile()

class RoomFileReference(
    @JvmField val loadTask: String,                                 // [file-load-task-name]
    @JvmField val fileName: String,                                 // [file-name]
    @JvmField val tileSetFilePath: String = EMPTY_STRING,           // [file-path]
    @JvmField val roomActivationScene: String = NO_NAME,            // [scene-name]
    @JvmField val roomDeactivationScene: String = NO_NAME,          // [scene-name]
)

class RoomJson(
    override val type: String = "room",                             // [type]
    @JvmField val name: String,                                     // [room-name]
    @JvmField val bounds: String,                                   // [x,y,width,height]
    @JvmField val props: String?,                                   // [attr1=v1|attr2=v2|...]
    @JvmField val tilesetFiles: Array<TileSetFile>,
    @JvmField val maps: Array<TileMapJson>,
    @JvmField val objects: Array<RoomObjectJson>
) : JsonFile()

class RoomObjectJson(
    @JvmField val objectType: String,
    @JvmField val buildTask: String,                    // [taskName]
    @JvmField val props: String?,                       // [attr1=v1|attr2=v2|...]
)


class TileMapJson(
    @JvmField val layerName: String,                    // [layer-name]
    @JvmField val opacity: String,                      // [opacity]
    @JvmField val tileWidth: Int,                       // [tile-width pixels]
    @JvmField val tileHeight: Int,                      // [tile-height pixels]
    @JvmField val dimension: String,                    // [x-offset pixel,y-offset pixel,width tiles,height tiles]
    @JvmField val infinite: Boolean,                    // [true|false]
    @JvmField val blendMode: String,                    // [blend-mode]
    @JvmField val tintColor: String,                    // [tint-color]
    @JvmField val parallax: String,                     // [parallax[x,y] pixels]
    @JvmField val sets: String,                         // [tileSetName1,tileSetName2...]
    @JvmField val props: String?,                       // [attr1=v1|attr2=v2|...]
    @JvmField val data: String                          // [map-data]
)

class TileSetJson(
    override val type: String = "tileset",
    @JvmField val name: String,                         // [Name]
    @JvmField val atlas: String,                        // [path/file]
    @JvmField val collision: String?,                   // [path/file]
    @JvmField val dimension: String,                    // [tile-width,tile-height,width,height]
    @JvmField val tiles: Array<TileJson>,
) : JsonFile()

class TileSetFile(
    @JvmField val name: String,
    @JvmField val offset: Int,
    @JvmField val file: String
)

class TileJson(
    @JvmField val id: String,               // [index||name]
    @JvmField val props: String,            // [AtlasPosition[x,y]]|[Flipping[-||v||h||vh]]|[BlendMode]|[BlendColor]|
                                            // [Contact[type,material]]|[ContactMaskPosition[x,y]]|[TileAspects]
    @JvmField val animation: String?        // [milliseconds,x,y,[Flipping[-||v||h||vh]]|[...]
)

object JsonAreaLoadTask : StaticTask() {

    init {
        JsonRoomLoadTask
        TiledRoomLoadTask
    }

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {
        // get all needed attributes and check
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]
        // load tiled JSON resource
        val areaJson = Engine.resourceService.loadJSONResource(res, AreaJson::class, enc)

        Area {
            name = areaJson.name
            loadScene(areaJson.loadScene)
            disposeScene(areaJson.disposeScene)
            loadInParallel = areaJson.loadInParallel
            stopLoadSceneWhenLoadFinished = areaJson.stopLoadSceneWhenLoadFinished
            activateAfterLoadScene = areaJson.activateAfterLoadScene
            roomViewName = areaJson.view
            roomCameraName = areaJson.camera
            roomActivationSceneName = areaJson.roomActivationScene
            roomDeactivationSceneName = areaJson.roomDeactivationScene

            val roomIt = areaJson.rooms.iterator()
            while (roomIt.hasNext()) {
                val roomJson = roomIt.next()

                val roomLoadAttrs = Attributes() + ( ATTR_RESOURCE to roomJson.fileName)
                if (roomJson.tileSetFilePath != EMPTY_STRING)
                    roomLoadAttrs + ( ATTR_TILE_SET_DIR_PATH to roomJson.tileSetFilePath )
                if (roomJson.roomActivationScene != NO_NAME)
                    roomLoadAttrs + ( ATTR_ACTIVATION_SCENE to roomJson.roomActivationScene )
                if (roomJson.roomDeactivationScene != NO_NAME)
                    roomLoadAttrs + ( ATTR_DEACTIVATION_SCENE to roomJson.roomDeactivationScene )

                withRoomLoadTask(roomLoadAttrs, Task[roomJson.loadTask])
            }
        }
    }

}


object JsonRoomLoadTask : StaticTask() {

    init {
        JsonTileSetLoadTask
        TiledTileSetLoadTask
    }

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {
        // get all needed attributes and check
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]
        // load tiled JSON resource
        val roomJson = Engine.resourceService.loadJSONResource(res, RoomJson::class, enc)

        loadTileSets(attributes, roomJson)
        loadRoom(key, attributes, roomJson)

        callback(NO_COMPONENT_KEY, attributes, OperationResult.SUCCESS)
    }

    fun loadTileSets(attributes: AttributesRO, roomJson: RoomJson) {
        val tsIt = roomJson.tilesetFiles.iterator()
        while (tsIt.hasNext()) {
            val tileSetRef = tsIt.next()
            if (TileSet.exists(tileSetRef.name))
                continue

            val tiledTileSetAttrs = Attributes() +
                    ( ATTR_TILE_SET_NAME to tileSetRef.name ) +
                    ( ATTR_RESOURCE to tileSetRef.file) +
                    ( ATTR_APPLY_ANIMATION_GROUPS to Room.ROOM_PAUSE_GROUP.aspectName)

            JsonTileSetLoadTask(attributes = tiledTileSetAttrs)
            val tileSet = TileSet[tileSetRef.name]
            tileSet.mappingStartTileId = tileSetRef.offset
        }
    }

    fun loadRoom(key : ComponentKey, attributes: AttributesRO, roomJson: RoomJson) {
        val areaKey = if (key != NO_COMPONENT_KEY && key.type == Area) key else null
        val view = Area.getDefaultView(key)
        val viewName = attributes[ATTR_VIEW_NAME] ?:
                view?.name ?:
                throw IllegalArgumentException("Missing view attribute")
        val activationTasks: MutableList<String> = attributes[ATTR_ACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()
        val deactivationTasks: MutableList<String> = attributes[ATTR_DEACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()

        // now create Room, TileMap and Objects from TiledMap input
        val roomKey = Room {
            this.name = roomJson.name
            areaRef(areaKey ?: NO_COMPONENT_KEY)
            // set attributes
            val attrs = Attributes()
            if (roomJson.props != null)
                attrs.addAll(roomJson.props)
            this.attributes = attrs

            roomBounds(roomJson.bounds)
            // tasks
            activationTasks.addAll(attributes[PROP_ROOM_ACTIVATION_TASKS]?.split(VALUE_SEPARATOR)
                ?: emptyList())
            deactivationTasks.addAll(attributes[PROP_ROOM_DEACTIVATION_TASKS]?.split(VALUE_SEPARATOR)
                ?: emptyList())
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

            if (attributes.contains(ATTR_ACTIVATION_SCENE))
                activationScene(attributes[ATTR_ACTIVATION_SCENE]!!)
            else if (areaRef.exists && Area[areaRef].roomActivationSceneName != NO_NAME)
                activationScene(Area[areaRef].roomActivationSceneName)
            if (attributes.contains(ATTR_DEACTIVATION_SCENE))
                deactivationScene(attributes[ATTR_DEACTIVATION_SCENE]!!)
            else if (areaRef.exists && Area[areaRef].roomDeactivationSceneName != NO_NAME)
                deactivationScene(Area[areaRef].roomDeactivationSceneName)
        }

        val tileMapKey = TileMap {
            this.name = roomJson.name
            viewRef(viewName)
        }
        val tileMap = TileMap[tileMapKey]
        val room = Room[roomKey]
        room.withLifecycleComponent(tileMapKey)

        // load tile maps
        val mit = roomJson.maps.iterator()
        while (mit.hasNext())
            loadTileMap(tileMap, roomJson, mit.next())
        // load object
        val oit = roomJson.objects.iterator()
        while (oit.hasNext())
            loadObject(viewName, room, oit.next())
    }

    private fun loadObject(viewName: String, room: Room, objectJson: RoomObjectJson) {
        val attributes = Attributes()
        if (objectJson.props != null)
            attributes.addAll(objectJson.props)

        attributes[ATTR_OBJECT_VIEW] = viewName
        val order = attributes[ATTR_OBJECT_ORDER]?.toInt() ?: 10

        room.withLifecycleTask {
            this.order = order
            this.attributes = attributes
            lifecycleType = LifecycleTaskType.ON_ACTIVATION
            task(objectJson.buildTask)
        }
    }

    private fun loadTileMap(tileMap: TileMap, roomJson: RoomJson, tileMapJson: TileMapJson) {
        val attributes = Attributes()
        if (tileMapJson.props != null)
            attributes.addAll(tileMapJson.props)

        val defaultBlend = BlendMode.valueOf(
            TiledRoomLoadTask.attributes[ATTR_DEFAULT_BLEND]
            ?: BlendMode.NONE.name)
        val defaultTint = Vector4f()(
            TiledRoomLoadTask.attributes[ATTR_DEFAULT_TINT]
            ?: WHITE.instance().toJsonString())

        // tile-sets mapping
        val layerTileSets = tileMapJson.sets.split(LIST_VALUE_SEPARATOR)
        val roomDim = Vector4i()(roomJson.bounds)
        val mapDim = Vector4i()(tileMapJson.dimension)
        val parallax = Vector2f()(tileMapJson.parallax)
        val data: IntArray = tileMapJson.data.split(COMMA).map(String::toInt).toIntArray()

        tileMap.withTileLayer {
            position(roomDim.x + mapDim.x, roomDim.y + mapDim.y)
            parallaxFactorX = parallax.x - 1
            parallaxFactorY = parallax.y - 1
            layerRef(tileMapJson.layerName)
            if (tileMapJson.tintColor != JSON_NO_VALUE)
                tint(GeomUtils.colorOf(tileMapJson.tintColor))
            else
                tint(defaultTint)
            if (tileMapJson.opacity != JSON_NO_VALUE)
                tint.a = tileMapJson.opacity.toFloat()
            blend = if (tileMapJson.blendMode != JSON_NO_VALUE)
                BlendMode.valueOf(tileMapJson.blendMode)
            else
                defaultBlend

            withTileGridData {
                tileWidth = tileMapJson.tileWidth
                tileHeight = tileMapJson.tileHeight
                mapWidth = mapDim.width
                mapHeight = mapDim.height
                mapCodes = data
                spherical = tileMapJson.infinite
                if (RENDERER_PROP in attributes)
                    renderer = ViewSystemRenderer.byName(attributes[RENDERER_PROP]!!)

                // define tile sets for this map
                val iter =  layerTileSets.iterator()
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
}

object JsonTileSetLoadTask : StaticTask() {

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {

        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]

        load(attributes, Engine.resourceService.loadJSONResource(res, TileSetJson::class, enc))
        callback(NO_COMPONENT_KEY, attributes, OperationResult.SUCCESS)
    }

    internal fun load(attributes: AttributesRO, tileSetJson: TileSetJson) {
        val name = attributes[ATTR_TILE_SET_NAME] ?: throw IllegalArgumentException("Missing name")
        val startId = attributes[ATTR_MAPPING_START_TILE_ID]?.toInt() ?: -1
        val tileSetGroups = attributes[ATTR_APPLY_TILE_SET_GROUPS]?.split(LIST_VALUE_SEPARATOR)
        val animationGroups = attributes[ATTR_APPLY_ANIMATION_GROUPS] ?: NO_NAME
        val dim = Vector4i()(tileSetJson.dimension)

        // create atlas texture asset for the tile set if not existing
        if (!Texture.exists(tileSetJson.atlas))
            Texture {
                this.name = tileSetJson.atlas
                resourceName = tileSetJson.atlas
            }
        Texture.activate(tileSetJson.atlas)
        var collisionTexRef = NO_COMPONENT_KEY
        if (tileSetJson.collision != null) {
            if (!Texture.exists(tileSetJson.collision))
                AccessibleTexture {
                    this.name = tileSetJson.collision
                    resourceName = tileSetJson.collision
                }
            Asset.activate(tileSetJson.collision)
            collisionTexRef = Asset.getKey(tileSetJson.collision)
        }

        // create TileSet
        TileSet {
            this.name = name
            tileSetGroups?.forEach { groups + it }
            textureRef(tileSetJson.atlas)
            mappingStartTileId = startId
            // create tiles
            val iter = tileSetJson.tiles.iterator()
            while (iter.hasNext()) {
                val tileJson = iter.next()
                val props = tileJson.props.split(LIST_VALUE_SEPARATOR)
                val aPos = Vector2f()(props[0])
                val spriteFlip = props[1]
                val blend = BlendMode.valueOf(props[2])
                val tint = TileUtils.getColorFromString(props[3])
                val contact = props[4].split(VALUE_SEPARATOR)
                val contactType = if (contact[0] != JSON_NO_VALUE)
                    EContact.CONTACT_TYPE_ASPECT_GROUP[contact[0]] ?: EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect(contact[0])
                else UNDEFINED_CONTACT_TYPE
                val materialType = if (contact[1] != JSON_NO_VALUE)
                    EContact.MATERIAL_ASPECT_GROUP[contact[1]] ?: EContact.MATERIAL_ASPECT_GROUP.createAspect(contact[1])
                else UNDEFINED_MATERIAL
                val contactMaskPos =  Vector2i()(props[5])
                val fullContact = contactMaskPos.x < 0 || contactMaskPos.y < 0

                val contactBitMask = if (!fullContact)
                    TileUtils.loadTileContactBitMask(
                        collisionTexRef,
                        Vector4i(contactMaskPos.x, contactMaskPos.y, dim.x, dim.y)
                    ) else null
                val tileAspectsString = props[6]

                withTile {
                    this.name = tileJson.id
                    this.blendMode = blend
                    this.tintColor = tint
                    this.material = materialType
                    this.contactType = contactType
                    this.contactMask = contactBitMask

                    if (tileAspectsString != JSON_NO_VALUE) {
                        val iter = tileAspectsString.split(VALUE_SEPARATOR)?.iterator()
                        while (iter?.hasNext() == true)
                            this.aspects + iter.next()
                    }

                    this.withSprite {
                        this.textureBounds(
                            aPos.x * dim.x,
                            aPos.y * dim.y,
                            dim.x.toFloat(),
                            dim.y.toFloat()
                        )
                        this.hFlip = spriteFlip.contains(FLAG_NAME_HORIZONTAL)
                        this.vFlip = spriteFlip.contains(FLAG_NAME_VERTICAL)
                    }

                    if (tileJson.animation != null) {
                        val animFrames = tileJson.animation.split(LIST_VALUE_SEPARATOR)
                        this.groups = animationGroups

                        withAnimation {
                            var index = 0
                            while (index < animFrames.size) {
                                val animProps = animFrames[index].split(VALUE_SEPARATOR)
                                withFrame {
                                    interval = animProps[0].toLong()
                                    spriteTemplate {
                                        this.name = tileJson.id + "_$index"
                                        textureBounds(
                                            animProps[1].toInt() * dim.x,
                                            animProps[2].toInt() * dim.y,
                                            dim.x,
                                            dim.y
                                        )
                                        hFlip = animProps[3].contains(FLAG_NAME_HORIZONTAL)
                                        vFlip = animProps[3].contains(FLAG_NAME_VERTICAL)
                                    }
                                }
                                index++
                            }
                        }
                    }
                }
            }
        }
    }
}

object TiledRoomConversionTask : StaticTask() {

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing ATTR_RESOURCE_PATH")
        val enc = attributes[ATTR_ENCRYPTION]
        val target = attributes[ATTR_TARGET_FILE] ?: throw IllegalArgumentException("Missing ATTR_TARGET_FILE")


        Engine.resourceService.writeJSNONResource(
            target,
            convert(
                attributes,
                Engine.resourceService.loadJSONResource(res, TiledMapJson::class, enc)),
            enc)
    }

    internal fun convert(attributes: AttributesRO, tiledRoomJson: TiledMapJson): RoomJson {
        val name = tiledRoomJson.mappedProperties[NAME_PROP]?.stringValue ?: throw IllegalArgumentException("Missing name")
        val roomProps = getPropertiesString(tiledRoomJson.properties)

        // name --> (offset, file)
        val tileSetMapping: MutableList<TileSetFile> = mutableListOf()
        val tileSetRefs = tiledRoomJson.mappedProperties[PROP_TILE_SET_REFS]?.stringValue
            ?.split(LIST_VALUE_SEPARATOR)
            ?: throw IllegalArgumentException("Missing tileset_refs from TiledMap JSON file")
        val tileSetPath = attributes[ATTR_TILE_SET_DIR_PATH] ?: EMPTY_STRING
        val tsIt = tileSetRefs.iterator()
        var index = 0
        while (tsIt.hasNext()) {
            val tsName = tsIt.next()
            val overrideName = "$ATTR_TILESET_OVERRIDE_PREFIX$tsName"
            val d = tiledRoomJson.tilesets[index]
            val source =
                if (attributes.contains(overrideName)) attributes[overrideName]!!
                else tileSetPath + d.source.substringAfterLast('/')
            tileSetMapping.add(TileSetFile(tsName, d.firstgid, source))
            index++
        }

        val maps: MutableList<TileMapJson> = mutableListOf()
        val objects: MutableList<RoomObjectJson> = mutableListOf()
        val it = tiledRoomJson.layers.iterator()
        while (it.hasNext()) {
            val layer = it.next()
            if (layer.type == PROP_VALUE_TYPE_LAYER)
                maps.add(getMap(tiledRoomJson, layer))
            else if(layer.type == PROP_VALUE_TYPE_OBJECT)
                objects.addAll(getObjects(tiledRoomJson, layer))
        }
        val bounds = "0${COMMA}0" +
                "$COMMA${tiledRoomJson.width * tiledRoomJson.tilewidth}" +
                "$COMMA${tiledRoomJson.height * tiledRoomJson.tileheight}"

        return RoomJson(
            name = name,                                                            // [room-name]
            bounds = bounds,                                                        // [x,y,width,height]
            props = roomProps,                                                      // [attr1=v1,attr2=v2,...]
            tilesetFiles = tileSetMapping.toTypedArray(),
            maps = maps.toTypedArray(),
            objects = objects.toTypedArray()
        )
    }

    private fun getMap(tiledRoomJson: TiledMapJson, tiledLayer: TiledLayer): TileMapJson {
        val blend = tiledLayer.mappedProperties[BLEND_PROP]?.stringValue
            ?: JSON_NO_VALUE
        val tint = tiledLayer.mappedProperties[TINT_PROP]?.stringValue
            ?: JSON_NO_VALUE
        val parallax = "${tiledLayer.parallaxx}$COMMA${tiledLayer.parallaxy}"
        val layerTileSets = tiledLayer.mappedProperties[PROP_LAYER_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        val dimension = "${tiledLayer.offsetx}" +
                "$COMMA${tiledLayer.offsety}" +
                "$COMMA${tiledRoomJson.width}" +
                "$COMMA${tiledRoomJson.height}"
        val props = getPropertiesString(tiledLayer.properties)
        val data = tiledLayer.data?.joinToString(COMMA)
            ?: throw RuntimeException("Missing data for layer")

        return TileMapJson(
            layerName = tiledLayer.name,                        // [layer-name]]
            opacity = tiledLayer.opacity.toString(),            // [opacity]
            tileWidth = tiledRoomJson.tilewidth,                // [tile-width pixels]
            tileHeight = tiledRoomJson.tileheight,              // [tile-height pixels]
            dimension = dimension,                              // [x,y,width tiles,height tiles]
            infinite = tiledRoomJson.infinite,
            blendMode = blend,                                  // [blend-mode]
            tintColor = tint,                                   // [tint-color]
            parallax = parallax,                                // [parallax[x,y] pixels]
            sets = layerTileSets,                               // [tileSetName1,tileSetName2...]
            props = props,                                      // [attr1=v1|attr2=v2|...]
            data = data                                         // [map-data]
        )
    }

    private fun getObjects(tiledRoomJson: TiledMapJson, tiledLayer: TiledLayer): Array<RoomObjectJson> =
        tiledLayer.objects?.map{
            getObject(tiledRoomJson, it)
        }?.toTypedArray() ?: emptyArray()

    private fun getObject(tiledRoomJson: TiledMapJson, tiledObject: TiledObject): RoomObjectJson {

        val dim = "${tiledObject.x},${tiledObject.y},${tiledObject.width},${tiledObject.height}"
        val rot = tiledObject.rotation.toString()
        val scale = "${ tiledRoomJson.mappedProperties[SCALE_X] ?: JSON_NO_VALUE },${ tiledRoomJson.mappedProperties[SCALE_Y] ?: JSON_NO_VALUE }"
        val props = getPropertiesString(tiledObject.properties) +
                "$LIST_VALUE_SEPARATOR$ATTR_OBJECT_BOUNDS$KEY_VALUE_SEPARATOR$dim" +
                "$LIST_VALUE_SEPARATOR$ATTR_OBJECT_ROTATION$KEY_VALUE_SEPARATOR$rot" +
                "$LIST_VALUE_SEPARATOR$ATTR_OBJECT_SCALE$KEY_VALUE_SEPARATOR$scale"

        val buildTask = tiledObject.mappedProperties[PROP_OBJECT_BUILD_TASK]?.stringValue
            ?: throw IllegalArgumentException("Missing object build task property")

        return RoomObjectJson(
            objectType = tiledObject.type,          // [typeName]
            buildTask = buildTask,                  // [taskName]
            props = props,                          // [attr1=v1|attr2=v2|...]
        )
    }

    private fun getPropertiesString(tiledProps: Array<TiledPropertyJson>): String {
        var properties = JSON_NO_VALUE
        val it = tiledProps.iterator()
        val excludes = setOf(NAME_PROP, PROP_TILE_SET_REFS, PROP_LAYER_TILE_SETS)
        while (it.hasNext()) {
            val p = it.next()
            if (p.stringValue != EMPTY_STRING && p.name !in excludes) {
                if (properties == JSON_NO_VALUE)
                    properties = "${p.name}$KEY_VALUE_SEPARATOR${p.stringValue}"
                else
                    properties += "$LIST_VALUE_SEPARATOR${p.name}$KEY_VALUE_SEPARATOR${p.stringValue}"
            }
        }
        return properties
    }
}

object TiledTileSetConversionTask : StaticTask() {

    override fun apply(key : ComponentKey, attributes: AttributesRO, callback: TaskCallback) {
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing ATTR_RESOURCE_PATH")
        val enc = attributes[ATTR_ENCRYPTION]
        val target = attributes[ATTR_TARGET_FILE] ?: throw IllegalArgumentException("Missing ATTR_TARGET_FILE")

        Engine.resourceService.writeJSNONResource(
            target,
            convert(Engine.resourceService.loadJSONResource(res, TiledTileSetJson::class, enc)),
            enc)
    }

    internal fun convert(tiledTileSetJson: TiledTileSetJson): TileSetJson {
        val atlasPath = tiledTileSetJson.mappedProperties[ATLAS_PROP]?.stringValue
            ?: throw RuntimeException("Missing atlas texture resource")
        val collisionMapPath = tiledTileSetJson.mappedProperties[COLLISION_PROP]?.stringValue

        val tiles: MutableList<TileJson> = mutableListOf()
        val it = tiledTileSetJson.tiles.iterator()
        while (it.hasNext())
            tiles.add(createTileJson(it.next()))

        return TileSetJson(
            name = tiledTileSetJson.name,
            atlas = atlasPath,
            collision =  collisionMapPath,
            dimension = "${tiledTileSetJson.tilewidth}" +
                    "$COMMA${tiledTileSetJson.tileheight}" +
                    "$COMMA${tiledTileSetJson.columns}" +
                    "$COMMA${tiledTileSetJson.tilecount}",
            tiles = tiles.toTypedArray()
        )
    }

    private fun createTileJson(tiledTile: TiledTileJson): TileJson {
        return TileJson(
            id = getId(tiledTile),
            props = getProps(tiledTile),
            animation = getAnimation(tiledTile)
        )
    }

    private fun getId(tiledTile: TiledTileJson): String =
        tiledTile.mappedProperties[NAME_PROP]?.toString() ?: tiledTile.id.toString()

    private fun getProps(tiledTile: TiledTileJson): String {
        val blend = BlendMode.valueOf(tiledTile.mappedProperties[BLEND_PROP]?.stringValue ?: BlendMode.NONE.name)
        val tint = tiledTile.mappedProperties[TINT_PROP] ?: JSON_NO_VALUE
        val atlasX = tiledTile.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_X_PROP) ?: -1
        val atlasY = tiledTile.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_Y_PROP) ?: -1
        var spriteFlip = tiledTile.mappedProperties[ATLAS_FLIP_PROP]?.stringValue ?: JSON_NO_VALUE
        if (spriteFlip.contains(COMMA)) {
            val split = spriteFlip.split(COMMA)
            spriteFlip = "${split[0][0]}${split[1][0]}"
        } else if (spriteFlip != JSON_NO_VALUE)
            spriteFlip = spriteFlip[0].toString()


        val materialType = tiledTile.getSubPropAsString(MATERIAL_PROP, MATERIAL_TYPE_PROP) ?: JSON_NO_VALUE
        val contactType = tiledTile.getSubPropAsString(MATERIAL_PROP, CONTACT_TYPE_PROP) ?: JSON_NO_VALUE
        val contactBitmaskX = tiledTile.getSubPropAsInt(MATERIAL_PROP, CONTACT_X_PROP) ?: -1
        val contactBitmaskY = tiledTile.getSubPropAsInt(MATERIAL_PROP, CONTACT_Y_PROP) ?: -1
        val aspects = tiledTile.mappedProperties[ASPECT_PROP]?.stringValue ?: JSON_NO_VALUE

        return "$atlasX$COMMA$atlasY" +
                "$LIST_VALUE_SEPARATOR$spriteFlip" +
                "$LIST_VALUE_SEPARATOR$blend" +
                "$LIST_VALUE_SEPARATOR$tint" +
                "$LIST_VALUE_SEPARATOR$contactType$COMMA$materialType" +
                "$LIST_VALUE_SEPARATOR$contactBitmaskX$COMMA$contactBitmaskY" +
                "$LIST_VALUE_SEPARATOR$aspects"
    }

    private fun getAnimation(tiledTile: TiledTileJson): String? =
        if (tiledTile.mappedProperties.contains(ANIMATION_PROP))
            tiledTile.mappedProperties[ANIMATION_PROP]!!.stringValue.split(COMMA).map {
                val timeSprite = it.split(UNDERLINE)
                val spriteProps = timeSprite[1].split(COLON)
                val flip = if (spriteProps.size > 2) spriteProps[2] else JSON_NO_VALUE
                "${timeSprite[0]},${spriteProps[0]},${spriteProps[1]},$flip"
            }.joinToString(LIST_VALUE_SEPARATOR_STRING)
         else null

}