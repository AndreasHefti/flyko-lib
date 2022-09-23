package com.inari.firefly.game.tile

import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.game.tile.TileUtils.TILE_ASPECT_GROUP
import com.inari.firefly.graphics.sprite.SpriteFrame
import com.inari.firefly.graphics.sprite.SpriteTemplate
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.physics.contact.EContact.Companion.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.physics.contact.EContact.Companion.MATERIAL_ASPECT_GROUP
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.physics.contact.EContact.Companion.UNDEFINED_MATERIAL
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.BitMask
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

enum class TileDimType {
    EIGHT,
    SIXTEEN,
    THIRTY_TWO
}

enum class TileContactFormType(private val aspect: Aspect) : Aspect {
    UNDEF(CONTACT_TYPE_ASPECT_GROUP.createAspect("UNDEF")),
    QUAD(CONTACT_TYPE_ASPECT_GROUP.createAspect("QUAD")),
    RECTANGLE(CONTACT_TYPE_ASPECT_GROUP.createAspect("RECTANGLE")),
    SLOPE(CONTACT_TYPE_ASPECT_GROUP.createAspect("SLOPE")),
    SPIKE(CONTACT_TYPE_ASPECT_GROUP.createAspect("SPIKE")),
    CIRCLE(CONTACT_TYPE_ASPECT_GROUP.createAspect("CIRCLE")),
    RHOMBUS(CONTACT_TYPE_ASPECT_GROUP.createAspect("RHOMBUS"))
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}

enum class TileSizeType(private val aspect: Aspect) : Aspect {
    EMPTY(TILE_ASPECT_GROUP.createAspect("EMPTY")),
    FULL(TILE_ASPECT_GROUP.createAspect("FULL")),
    HALF(TILE_ASPECT_GROUP.createAspect("HALF")),
    QUARTER(TILE_ASPECT_GROUP.createAspect("QUARTER")),
    QUARTER_TO(TILE_ASPECT_GROUP.createAspect("QUARTER_TO"))
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}

enum class TileOrientation(private val aspect: Aspect) : Aspect {
    NONE(TILE_ASPECT_GROUP.createAspect("NONE_ORIENTATION")),
    HORIZONTAL(TILE_ASPECT_GROUP.createAspect("HORIZONTAL")),
    VERTICAL(TILE_ASPECT_GROUP.createAspect("VERTICAL"))
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}

enum class TileDirection(private val aspect: Aspect) : Aspect {
    NONE(TILE_ASPECT_GROUP.createAspect("NONE_DIRECTION")),
    NORTH_WEST(TILE_ASPECT_GROUP.createAspect("NORTH_WEST")),
    NORTH(TILE_ASPECT_GROUP.createAspect("NORTH")),
    NORTH_EAST(TILE_ASPECT_GROUP.createAspect("NORTH_EAST")),
    EAST(TILE_ASPECT_GROUP.createAspect("EAST")),
    SOUTH_EAST(TILE_ASPECT_GROUP.createAspect("SOUTH_EAST")),
    SOUTH(TILE_ASPECT_GROUP.createAspect("SOUTH")),
    SOUTH_WEST(TILE_ASPECT_GROUP.createAspect("SOUTH_WEST")),
    WEST(TILE_ASPECT_GROUP.createAspect("WEST"))
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}

enum class TileMaterialType(private val aspect: Aspect) : Aspect {
    NONE(MATERIAL_ASPECT_GROUP.createAspect("NONE")),
    BACKGROUND(MATERIAL_ASPECT_GROUP.createAspect("BACKGROUND")),
    TERRAIN_SOLID(MATERIAL_ASPECT_GROUP.createAspect("TERRAIN_SOLID")),
    TERRAIN_SEMI_SOLID(MATERIAL_ASPECT_GROUP.createAspect("TERRAIN_SOLID")),
    PROJECTILE(MATERIAL_ASPECT_GROUP.createAspect("PROJECTILE")),
    WATER(MATERIAL_ASPECT_GROUP.createAspect("WATER")),
    CLOUD(MATERIAL_ASPECT_GROUP.createAspect("CLOUD")),
    LAVA(MATERIAL_ASPECT_GROUP.createAspect("LAVA")),
    LADDER(MATERIAL_ASPECT_GROUP.createAspect("LADDER")),
    ROPE(MATERIAL_ASPECT_GROUP.createAspect("ROPE")),
    INTERACTIVE(MATERIAL_ASPECT_GROUP.createAspect("INTERACTIVE")),
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}

@ComponentDSL
class TileTemplate internal constructor() {

    @JvmField internal val spriteTemplate = SpriteTemplate()
    @JvmField internal var animationData: TileAnimation? = null

    @JvmField var name: String = NO_NAME
    @Suppress("SetterBackingFieldAssignment")
    var aspects: Aspects = TILE_ASPECT_GROUP.createAspects()
        set(value) {
            field.clear()
            field + value
        }
    @JvmField var material: Aspect = UNDEFINED_MATERIAL
    @JvmField var contactType: Aspect = UNDEFINED_CONTACT_TYPE
    @JvmField var contactMask: BitMask? = null
    @JvmField var tintColor: Vector4f? = null
    @JvmField var blendMode: BlendMode? = null
    @JvmField val withAnimation: (TileAnimation.() -> Unit) -> Unit = { configure ->
        animationData = TileAnimation()
        animationData!!.also(configure)
    }
    @JvmField val withSprite: (SpriteTemplate.() -> Unit) -> Unit = { configure ->
        spriteTemplate.also(configure)
    }

    val hasContactComp: Boolean
        get() = contactType !== UNDEFINED_CONTACT_TYPE ||
                material !== UNDEFINED_MATERIAL
}

@ComponentDSL
class TileAnimation internal constructor() {

    @JvmField internal val frames: DynArray<SpriteFrame> = DynArray.of(5, 5)
    @JvmField internal val sprites: MutableMap<String, SpriteTemplate> = mutableMapOf()

    val withFrame: (SpriteFrame.() -> Unit) -> Unit = { configure ->
        val frame = SpriteFrame()
        frame.also(configure)

        if (frame.sprite.name == NO_NAME)
            throw IllegalArgumentException("Missing name")

        frames.add(frame)
        sprites[frame.sprite.name] = frame.sprite
    }
}

@ComponentDSL
class TileDecoration {

    @JvmField var tileName = NO_NAME
    @JvmField var renderer: EntityRenderer? = null
    @JvmField var layer = CReference(Layer)
    var layerIndex = 0
        get() = layer.targetKey.instanceIndex
}

@ComponentDSL
class TileMapData {

    @JvmField internal val entityCodeMapping = DynIntArray(100, -1, 100)
    @JvmField internal val tileSetMapping = DynArray.of<TileSetMapping>(5, 5)
    @JvmField internal var tileGridIndex = -1

    @JvmField var renderer: EntityRenderer? = null
    @JvmField var mapWidth = 0
    @JvmField var mapHeight = 0
    @JvmField var tileWidth = 0
    @JvmField var tileHeight = 0
    @JvmField var parallaxFactorX = ZERO_FLOAT
    @JvmField var parallaxFactorY = ZERO_FLOAT
    @JvmField var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    @JvmField var spherical: Boolean = false
    @JvmField var blend = BlendMode.NORMAL_ALPHA
    @JvmField var tint = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var layer = CReference(Layer)
    @JvmField var mapCodes: IntArray = intArrayOf()
    val layerIndex: Int
        get() = if (layer.targetKey.instanceIndex >= 0) layer.targetKey.instanceIndex else 0

    val withTileSetMapping: (TileSetMapping.() -> Unit) -> Unit = { configure ->
        val instance = TileSetMapping()
        instance.also(configure)
        tileSetMapping.add(instance)
    }
}

class TileSetMapping {
    var tileSetRef = CReference(TileSet)
    val tileSetIndex: Int
        get() {
            val tileSet = TileSet[tileSetRef.targetKey]
            if (!tileSet.loaded)
                TileSet.load(tileSetRef.targetKey)
            return tileSet.index
        }
    var codeOffset = 1
}