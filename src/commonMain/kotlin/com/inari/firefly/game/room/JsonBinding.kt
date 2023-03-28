package com.inari.firefly.game.room

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.Engine
import com.inari.firefly.core.LifecycleTaskType
import com.inari.firefly.core.StaticTask
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.core.api.TaskCallback
import com.inari.firefly.game.*
import com.inari.firefly.game.world.Area
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

class TileJson(
    @JvmField val id: String,                           // [index||name]
    @JvmField val props: String,                        // [AtlasPosition[x,y]]|[Flipping[-||v||h||vh]]|[BlendMode]|[BlendColor]|
                                                        // [Contact[type,material]]|[ContactMaskPosition[x,y]]|[TileAspects]
    @JvmField val animation: String?                    // [milliseconds,x,y,[Flipping[-||v||h||vh]]|[...]
)
abstract class JsonFile {
    abstract val type: String                           // [FileType[tileset||room||area||world]]
}

class TileSetJson(
    override val type: String = "tileset",
    @JvmField val name: String,                         // [Name]
    @JvmField val atlas: String,                        // [path/file]
    @JvmField val collision: String?,                   // [path/file]
    @JvmField val dimension: String,                    // [tile-width,tile-height,width,height]
    @JvmField val tiles: Array<TileJson>,
) : JsonFile()

class TileMapJson(
    @JvmField val layerName: String,                    // [layer-name]
    @JvmField val opacity: String,                      // [opacity]
    @JvmField val tileWidth: Int,                       // [tile-width pixels]
    @JvmField val tileHeight: Int,                      // [tile-height pixels]
    @JvmField val dimension: String,                    // [x-offset pixel,y-offset pixel,width tiles,height tiles]
    @JvmField val infinite: Boolean,                     // [true|false]
    @JvmField val blendMode: String,                    // [blend-mode]
    @JvmField val tintColor: String,                    // [tint-color]
    @JvmField val parallax: String,                     // [parallax[x,y] pixels]
    @JvmField val sets: String,                         // [tileSetName1,tileSetName2...]
    @JvmField val props: String,                        // [attr1=v1|attr2=v2|...]
    @JvmField val data: String                          // [map-data]
)

class RoomObjectJson(
    @JvmField val objectType: String,
    @JvmField val buildTask: String,                    // [taskName]
    @JvmField val props: String,                        // [attr1=v1|attr2=v2|...]
)

class RoomJson(
    override val type: String = "room",                 // [room-type]
    @JvmField val name: String,                         // [room-name]
    @JvmField val bounds: String,                       // [x,y,width,height]
    @JvmField val props: String,                        // [attr1=v1|attr2=v2|...]
    @JvmField val tilesetFiles: Array<TileSetFile>,
    @JvmField val maps: Array<TileMapJson>,
    @JvmField val objects: Array<RoomObjectJson>
) : JsonFile()

class TileSetFile(
    @JvmField val name: String,
    @JvmField val offset: Int,
    @JvmField val file: String
)

object JsonRoomLoadTask : StaticTask() {

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

            //val resPath = tileSetDirPath + tileSetRefJson.source.substringAfterLast('/')
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
        val viewName = attributes[ATTR_VIEW_NAME] ?:
            if (key != NO_COMPONENT_KEY)
                Area[key].viewRef.targetKey.name
            else
                throw IllegalArgumentException("Missing view ref")
        val tileSetDirPath = attributes[ATTR_TILE_SET_DIR_PATH] ?: EMPTY_STRING
        val activationTasks: MutableList<String> = attributes[ATTR_ACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()
        val deactivationTasks: MutableList<String> = attributes[ATTR_DEACTIVATION_TASKS]
            ?.split(VALUE_SEPARATOR)?.toMutableList() ?: mutableListOf()
        val createLayer = attributes[ATTR_CREATE_LAYER]?.toBoolean() ?: true
        println(" name: ${roomJson.name}")
        // now create Room, TileMap and Objects from TiledMap input
        val roomKey = Room {
            this.name = roomJson.name
            if (key != NO_COMPONENT_KEY && key.type == Area)
                areaRef(key)
            // set attributes
            this.attributes = Attributes().addAll(roomJson.props)
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
            if (attributes.contains(ATTR_DEACTIVATION_SCENE))
                deactivationScene(attributes[ATTR_DEACTIVATION_SCENE]!!)
        }

        val tileMapKey = TileMap {
            this.name = roomJson.name
            viewRef(viewName)
            autoCreateLayer = createLayer
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
        val attributes = Attributes().addAll(objectJson.props)
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
        val attributes = Attributes().addAll(tileMapJson.props)
        val defaultBlend = BlendMode.valueOf(TiledRoomLoadTask.attributes[ATTR_DEFAULT_BLEND] ?: "NONE")
        val defaultTint = Vector4f()(TiledRoomLoadTask.attributes[ATTR_DEFAULT_TINT] ?: "1,1,1,1")

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
            if (tileMapJson.tintColor != NULL_VALUE)
                tint(GeomUtils.colorOf(tileMapJson.tintColor))
            else
                tint(defaultTint)
            if (tileMapJson.opacity != NULL_VALUE)
                tint.a = tileMapJson.opacity.toFloat()
            blend = if (tileMapJson.blendMode != NULL_VALUE)
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
            AccessibleTexture.activate(tileSetJson.collision)
            collisionTexRef = AccessibleTexture.getKey(tileSetJson.collision)
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
                val contactType = if (contact[0] != NULL_VALUE)
                    EContact.CONTACT_TYPE_ASPECT_GROUP[contact[0]] ?: EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect(contact[0])
                else UNDEFINED_CONTACT_TYPE
                val materialType = if (contact[1] != NULL_VALUE)
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

                    if (tileAspectsString != NULL_VALUE) {
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
        val bounds = "0,0,${tiledRoomJson.width * tiledRoomJson.tilewidth},${tiledRoomJson.height * tiledRoomJson.tileheight}"

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
            ?: NULL_VALUE
        val tint = tiledLayer.mappedProperties[TINT_PROP]?.stringValue
            ?: NULL_VALUE
        val parallax = "${tiledLayer.parallaxx},${tiledLayer.parallaxy}"
        val layerTileSets = tiledLayer.mappedProperties[PROP_LAYER_TILE_SETS]?.stringValue
            ?: throw RuntimeException("Missing tile sets for layer")

        val dimension = "${tiledLayer.offsetx},${tiledLayer.offsety},${tiledRoomJson.width},${tiledRoomJson.height}"
        val props = getPropertiesString(tiledLayer.properties)
        val data = tiledLayer.data?.joinToString(",")
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
        val scale = "${ tiledRoomJson.mappedProperties[SCALE_X] ?: NULL_VALUE },${ tiledRoomJson.mappedProperties[SCALE_Y] ?: NULL_VALUE }"
        val props = getPropertiesString(tiledObject.properties) +
                "|$ATTR_OBJECT_BOUNDS=$dim|$ATTR_OBJECT_ROTATION=$rot|$ATTR_OBJECT_SCALE=$scale"

        val buildTask = tiledObject.mappedProperties[PROP_OBJECT_BUILD_TASK]?.stringValue
            ?: throw IllegalArgumentException("Missing object build task property")

        return RoomObjectJson(
            objectType = tiledObject.type,          // [typeName]
            buildTask = buildTask,                  // [taskName]
            props = props,                          // [attr1=v1|attr2=v2|...]
        )
    }

    private fun getPropertiesString(tiledProps: Array<TiledPropertyJson>): String {
        var properties = NULL_VALUE
        val it = tiledProps.iterator()
        val excludes = setOf("name", "tileset_refs", "blend_mode", "layer_tilesets")
        while (it.hasNext()) {
            val p = it.next()
            if (p.stringValue != EMPTY_STRING && p.name !in excludes) {
                if (properties == NULL_VALUE)
                    properties = "${p.name}=${p.stringValue}"
                else
                    properties += "|${p.name}=${p.stringValue}"
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
            type = "tileset",
            name = tiledTileSetJson.name,
            atlas = atlasPath,
            collision =  collisionMapPath,
            dimension = "${tiledTileSetJson.tilewidth},${tiledTileSetJson.tileheight},${tiledTileSetJson.columns},${tiledTileSetJson.tilecount}",
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
        val tint = tiledTile.mappedProperties[TINT_PROP] ?: NULL_VALUE
        val atlasX = tiledTile.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_X_PROP) ?: -1
        val atlasY = tiledTile.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_Y_PROP) ?: -1
        var spriteFlip = tiledTile.mappedProperties[ATLAS_FLIP_PROP]?.stringValue ?: NULL_VALUE
        if (spriteFlip.contains(COMMA)) {
            val split = spriteFlip.split(COMMA)
            spriteFlip = "${split[0][0]}${split[1][0]}"
        } else if (spriteFlip != NULL_VALUE)
            spriteFlip = spriteFlip[0].toString()


        val materialType = tiledTile.getSubPropAsString(MATERIAL_PROP, MATERIAL_TYPE_PROP) ?: NULL_VALUE
        val contactType = tiledTile.getSubPropAsString(MATERIAL_PROP, CONTACT_TYPE_PROP) ?: NULL_VALUE
        val contactBitmaskX = tiledTile.getSubPropAsInt(MATERIAL_PROP, CONTACT_X_PROP) ?: -1
        val contactBitmaskY = tiledTile.getSubPropAsInt(MATERIAL_PROP, CONTACT_Y_PROP) ?: -1
        val aspects = tiledTile.mappedProperties[ASPECT_PROP]?.stringValue ?: NULL_VALUE

        return "$atlasX,$atlasY|$spriteFlip|$blend|$tint|$contactType,$materialType|$contactBitmaskX,$contactBitmaskY|$aspects"
    }

    private fun getAnimation(tiledTile: TiledTileJson): String? =
        if (tiledTile.mappedProperties.contains(ANIMATION_PROP))
            tiledTile.mappedProperties[ANIMATION_PROP]!!.stringValue.split(COMMA).map {
                val timeSprite = it.split(UNDERLINE)
                val spriteProps = timeSprite[1].split(COLON)
                val flip = if (spriteProps.size > 2) spriteProps[2] else NULL_VALUE
                "${timeSprite[0]},${spriteProps[0]},${spriteProps[1]},$flip"
            }.joinToString("|")
         else null

}