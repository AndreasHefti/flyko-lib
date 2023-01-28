package com.inari.firefly.game.tile.tiled_binding

import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.tile.TileMaterialType
import com.inari.firefly.game.tile.TileSet
import com.inari.firefly.game.tile.TileUtils
import com.inari.firefly.graphics.sprite.AccessibleTexture
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.physics.contact.EContact
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.util.*
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class TiledTileSet private constructor() : TileSet() {

    private var atlasAssetId = NO_COMPONENT_KEY
    private var collisionMaskAssetId = NO_COMPONENT_KEY
    private var resourceSupplier: () -> TiledTileSetJson = { throw RuntimeException() }

    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resourceSupplier = { Engine.resourceService.loadJSONResource(value, TiledTileSetJson::class, encryptionKey) }
        }

    fun withTileSetJson(tileSetJson: TiledTileSetJson) {
        resourceSupplier = { tileSetJson }
    }

    override fun load() {
        val tileSetJson = resourceSupplier.invoke()

        val atlasPath = tileSetJson.mappedProperties[ATLAS_PROP]?.stringValue
            ?: throw RuntimeException("Missing atlas texture resource")
        val collisionMapPath = tileSetJson.mappedProperties[COLLISION_PROP]?.stringValue

        // create texture asset for the tile set if not existing
        val atlasAssetName = "$ATLAS_ASSET_NAME_PREFIX${this@TiledTileSet.name}"
        if (Texture.exists(atlasAssetName)) {
            atlasAssetId = Texture.getKey(atlasAssetName)
        }
        if (atlasAssetId == NO_COMPONENT_KEY) {
            atlasAssetId = Texture {
                name = atlasAssetName
                resourceName = atlasPath
            }
        }
        Texture.activate(atlasAssetId)
        // if there is also a collision mask defined check and create asset
        if (collisionMapPath != null) {
            val collisionAssetName = "$COLLISION_ASSET_NAME_PREFIX${this@TiledTileSet.name}"
            if (AccessibleTexture.exists(collisionAssetName)) {
                collisionMaskAssetId = AccessibleTexture.getKey(collisionAssetName)
            }
            if (collisionMaskAssetId == NO_COMPONENT_KEY) {
                collisionMaskAssetId = AccessibleTexture {
                    name = collisionAssetName
                    resourceName = collisionMapPath
                }
            }
            AccessibleTexture.activate(collisionMaskAssetId)
        }

        // create tile templates
        val tileDimType = TileUtils.getTileDimensionType(tileSetJson.tilewidth)
        // set atlas asset reference
        textureRef(this@TiledTileSet.atlasAssetId)
        // create tiles
        tileSetJson.tiles.forEach { tileJson ->
            val tileName = tileJson.mappedProperties[NAME_PROP]?.stringValue
                ?: NO_NAME
            val blend = tileJson.mappedProperties[BLEND_PROP]
                ?.let { BlendMode.valueOf(it.stringValue) }
                    ?: BlendMode.NONE
            val tint = tileJson.mappedProperties[TINT_PROP]
                ?.let {TileUtils.getColorFromString(it.stringValue) }
            val materialType = tileJson.getSubPropAsString(MATERIAL_PROP, MATERIAL_TYPE_PROP)
                ?.let { EContact.MATERIAL_ASPECT_GROUP[it] ?: TileMaterialType.NONE }
                ?: TileMaterialType.NONE
            val contactType = tileJson.getSubPropAsString(MATERIAL_PROP, CONTACT_TYPE_PROP)
                ?.let { EContact.CONTACT_TYPE_ASPECT_GROUP[it] ?: UNDEFINED_CONTACT_TYPE }
                ?: UNDEFINED_CONTACT_TYPE
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
            val contactBitMask = if (!fullContact)
                TileUtils.loadTileContactBitMask(
                    collisionMaskAssetId,
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

                tileAspectsString?.split(COMMA)?.forEach {
                    this.aspects + it
                }

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
                    val frames = tileJson.mappedProperties[ANIMATION_PROP]!!.stringValue.split(COMMA)
                    withAnimation {
                        frames.forEachIndexed { index, fString ->
                            val timeSprite = fString.split(UNDERLINE)
                            val spriteProps = timeSprite[1].split(COLON)
                            withFrame {
                                interval = timeSprite[0].toLong()
                                spriteTemplate {
                                    name = tileName + "_$index"
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

    override fun dispose() {
        tileTemplates.clear()
        atlasAssetId = NO_COMPONENT_KEY
    }

    companion object : ComponentSubTypeBuilder<TileSet, TiledTileSet>(TileSet,"TiledTileSet") {
        override fun create() = TiledTileSet()
    }
}