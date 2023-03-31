package com.inari.firefly.game.world

import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.game.world.TileUtils.TILE_ASPECT_GROUP
import com.inari.firefly.graphics.sprite.SpriteFrame
import com.inari.firefly.graphics.sprite.SpriteTemplate
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.firefly.graphics.view.Layer
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

enum class TileMaterialType(private val aspect: Aspect) : Aspect {
    NONE(MATERIAL_ASPECT_GROUP.createAspect("NONE")),
    TERRAIN(MATERIAL_ASPECT_GROUP.createAspect("TERRAIN")),
    PROJECTILE(MATERIAL_ASPECT_GROUP.createAspect("PROJECTILE")),
    WATER(MATERIAL_ASPECT_GROUP.createAspect("WATER")),
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
    @JvmField var groups: String = NO_NAME
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
class TileMapGridData {

    @JvmField internal val tileSetMapping = DynArray.of<TileSetMapping>(2, 5)

    @JvmField internal var tileGridIndex = NULL_COMPONENT_INDEX
    @JvmField var renderer: EntityRenderer? = null
    @JvmField var mapWidth = 0
    @JvmField var mapHeight = 0
    @JvmField var tileWidth = 0
    @JvmField var tileHeight = 0
    @JvmField var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    @JvmField var spherical: Boolean = false
    @JvmField var blend = BlendMode.NORMAL_ALPHA
    @JvmField var tint = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var mapCodes: IntArray = intArrayOf()

    val withTileSetMapping: (TileSetMapping.() -> Unit) -> Unit = { configure ->
        val instance = TileSetMapping()
        instance.also(configure)
        tileSetMapping.add(instance)
    }

}

@ComponentDSL
class TileMapLayerData {

    @JvmField internal val tileGridData = DynArray.of<TileMapGridData>(2, 5)
    @JvmField internal val entityCodeMapping = DynIntArray(50, NULL_COMPONENT_INDEX, 100)

    @JvmField var parallaxFactorX = ZERO_FLOAT
    @JvmField var parallaxFactorY = ZERO_FLOAT
    @JvmField var position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    @JvmField var blend = BlendMode.NORMAL_ALPHA
    @JvmField var tint = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var layerRef = CReference(Layer)

    val withTileGridData: (TileMapGridData.() -> Unit) -> Unit = { configure ->
        val instance = TileMapGridData()
        instance.also(configure)
        tileGridData.add(instance)
    }
}

class TileSetMapping {
    var tileSetRef = CReference(TileSet)
    val tileSetIndex: Int
        get() = tileSetRef.targetKey.componentIndex
    var codeOffset = 1
}