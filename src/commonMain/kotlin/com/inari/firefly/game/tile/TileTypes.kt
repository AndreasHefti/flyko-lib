package com.inari.firefly.game.tile

import com.inari.firefly.CONTACT_TYPE_ASPECT_GROUP
import com.inari.firefly.MATERIAL_ASPECT_GROUP
import com.inari.firefly.TILE_ASPECT_GROUP
import com.inari.util.aspect.*

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

