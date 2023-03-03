package com.inari.firefly.game.room.tiled_binding

import com.inari.firefly.core.Engine
import com.inari.firefly.core.StaticTask
import com.inari.firefly.core.api.*
import com.inari.firefly.game.room.TileMaterialType
import com.inari.firefly.game.room.TileSet
import com.inari.firefly.game.room.TileUtils
import com.inari.firefly.graphics.sprite.AccessibleTexture
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.physics.contact.EContact
import com.inari.util.*
import com.inari.util.collection.Dictionary
import com.inari.util.geom.Vector4i

object  TiledTileSetLoadTask : StaticTask() {

    const val ATTR_NAME = "tileSetName"
    const val ATTR_RESOURCE = "tiledJsonResource"
    const val ATTR_ENCRYPTION = "encryptionKey"
    const val ATTR_MAPPING_START_TILE_ID = "mappingStartTileId"
    const val ATTR_APPLY_TILE_SET_GROUPS = "applyTileSetGroups"
    const val ATTR_APPLY_ANIMATION_GROUPS = "applyAnimationGroups"

    override fun apply(attributes: Dictionary, callback: TaskCallback) {

        // get all needed attributes and check
        val name = attributes[ATTR_NAME] ?: throw IllegalArgumentException("Missing name")
        val res = attributes[ATTR_RESOURCE] ?: throw IllegalArgumentException("Missing resource")
        val enc = attributes[ATTR_ENCRYPTION]
        val startId = attributes[ATTR_MAPPING_START_TILE_ID]?.toInt() ?: -1

        // load tiled JSON resource
        val tileSetJson = Engine.resourceService.loadJSONResource(res, TiledTileSetJson::class, enc)
        // get atlas and collision mask for tileset
        val atlasPath = tileSetJson.mappedProperties[ATLAS_PROP]?.stringValue
            ?: throw RuntimeException("Missing atlas texture resource")
        val collisionMapPath = tileSetJson.mappedProperties[COLLISION_PROP]?.stringValue

        // create atlas texture asset for the tile set if not existing
        if (!Texture.exists(atlasPath))
            Texture {
                this.name = atlasPath
                resourceName = atlasPath
            }
        Texture.activate(atlasPath)
        var collisionTexRef = NO_COMPONENT_KEY
        if (collisionMapPath != null) {
            if (!Texture.exists(collisionMapPath))
                AccessibleTexture {
                    this.name = collisionMapPath
                    resourceName = collisionMapPath
                }
            AccessibleTexture.activate(collisionMapPath)
            collisionTexRef = AccessibleTexture.getKey(collisionMapPath)
        }

        val tileSetGroups = attributes[ATTR_APPLY_TILE_SET_GROUPS]?.split(LIST_VALUE_SEPARATOR)
        val animationGroups = attributes[ATTR_APPLY_ANIMATION_GROUPS] ?: NO_NAME

        // create TileSet
        TileSet {
            this.name = name
            tileSetGroups?.forEach { groups + it }
            textureRef(atlasPath)
            mappingStartTileId = startId
            // create tiles
            val iter = tileSetJson.tiles.iterator()
            while (iter.hasNext()) {
                val tileJson = iter.next()
                val tileName = tileJson.mappedProperties[NAME_PROP]?.stringValue
                    ?: NO_NAME
                val blend = tileJson.mappedProperties[BLEND_PROP]
                    ?.let { BlendMode.valueOf(it.stringValue) }
                    ?: BlendMode.NONE
                val tint = tileJson.mappedProperties[TINT_PROP]
                    ?.let { TileUtils.getColorFromString(it.stringValue) }
                val materialType = tileJson.getSubPropAsString(MATERIAL_PROP, MATERIAL_TYPE_PROP)
                    ?.let { EContact.MATERIAL_ASPECT_GROUP[it] ?: EContact.MATERIAL_ASPECT_GROUP.createAspect(it) }
                    ?: TileMaterialType.NONE
                val contactType = tileJson.getSubPropAsString(MATERIAL_PROP, CONTACT_TYPE_PROP)
                    ?.let { EContact.CONTACT_TYPE_ASPECT_GROUP[it] ?: EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect(it) }
                    ?: EContact.UNDEFINED_CONTACT_TYPE
                val atlasX = tileJson.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_X_PROP)
                    ?: -1
                val atlasY = tileJson.getSubPropAsInt(ATLAS_POS_PROP, ATLAS_Y_PROP)
                    ?: -1
                val spriteFlip = tileJson.mappedProperties[ATLAS_FLIP_PROP]?.stringValue
                    ?: EMPTY_STRING
                val fullContact = tileJson.getSubPropAsBoolean(MATERIAL_PROP, CONTACT_FULL_PROP)
                    ?: false
                val contactBitmaskX = tileJson.getSubPropAsInt(MATERIAL_PROP, CONTACT_X_PROP)
                    ?: -1
                val contactBitmaskY = tileJson.getSubPropAsInt(MATERIAL_PROP, CONTACT_Y_PROP)
                    ?: -1
                val contactBitMask = if (!fullContact && contactBitmaskX >= 0 && contactBitmaskY >= 0)
                    TileUtils.loadTileContactBitMask(
                        collisionTexRef,
                        Vector4i(contactBitmaskX, contactBitmaskY, tileSetJson.tilewidth, tileSetJson.tileheight)
                    ) else null
                val tileAspectsString = tileJson.mappedProperties[ASPECT_PROP]?.stringValue

                withTile {
                    this.name = tileName
                    this.blendMode = blend
                    this.tintColor = tint
                    this.material = materialType
                    this.contactType = contactType
                    this.contactMask = contactBitMask

                    val iter = tileAspectsString?.split(COMMA)?.iterator()
                    while (iter?.hasNext() == true)
                        this.aspects + iter.next()

                    this.withSprite {
                        this.textureBounds(
                            atlasX * tileSetJson.tilewidth,
                            atlasY * tileSetJson.tileheight,
                            tileSetJson.tilewidth,
                            tileSetJson.tileheight
                        )
                        this.hFlip = spriteFlip.contains(HORIZONTAL_VALUE)
                        this.vFlip = spriteFlip.contains(VERTICAL_VALUE)
                    }

                    if (tileJson.mappedProperties.contains(ANIMATION_PROP)) {
                        this.groups = animationGroups
                        val frames = tileJson.mappedProperties[ANIMATION_PROP]!!.stringValue.split(COMMA)
                        withAnimation {
                            frames.forEachIndexed { index, fString ->
                                val timeSprite = fString.split(UNDERLINE)
                                val spriteProps = timeSprite[1].split(COLON)
                                withFrame {
                                    interval = timeSprite[0].toLong()
                                    spriteTemplate {
                                        this.name = tileName + "_$index"
                                        textureBounds(
                                            spriteProps[0].toInt() * tileSetJson.tilewidth,
                                            spriteProps[1].toInt() * tileSetJson.tileheight,
                                            tileSetJson.tilewidth,
                                            tileSetJson.tileheight
                                        )
                                        hFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_HORIZONTAL))
                                        vFlip = spriteProps.size > 2 && (spriteProps[2].contains(FLAG_NAME_VERTICAL))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        callback(NO_COMPONENT_KEY, attributes, OperationResult.SUCCESS)
    }
}