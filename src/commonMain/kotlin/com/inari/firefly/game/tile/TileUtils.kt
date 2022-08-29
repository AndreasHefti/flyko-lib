package com.inari.firefly.game.tile

import com.inari.util.StringUtils
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.geom.BitMask
import com.inari.util.geom.Direction
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

object TileUtils {

    @JvmField val TILE_ASPECT_GROUP = IndexedAspectType("TILE_ASPECT_GROUP")
    @JvmField val UNDEFINED_TILE_ASPECT = TILE_ASPECT_GROUP.createAspect("UNDEFINED_TILE_ASPECT")

    fun createFullCircleBitMask_8(): BitMask =
        BitMask(0, 0, 8, 8).setBits(
            "0,0,0,1,1,0,0,0," +
            "0,0,1,1,1,1,0,0," +
            "0,1,1,1,1,1,1,0," +
            "1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1," +
            "0,1,1,1,1,1,1,0," +
            "0,0,1,1,1,1,0,0," +
            "0,0,0,1,1,0,0,0")

    fun createFullRhombusBitMask_8(): BitMask =
        BitMask(0, 0, 8, 8).setBits(
            "0,0,0,1,1,0,0,0," +
            "0,0,1,1,1,1,0,0," +
            "0,1,1,1,1,1,1,0," +
            "1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1," +
            "0,1,1,1,1,1,1,0," +
            "0,0,1,1,1,1,0,0," +
            "0,0,0,1,1,0,0,0")

    fun createFullCircleBitMask_16(): BitMask =
        BitMask(0, 0, 16, 16).setBits(
            "0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0," +
            "0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0," +
            "0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0," +
            "0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0," +
            "0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0," +
            "0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0," +
            "0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0")

    fun createFullRhombusBitMask_16(): BitMask =
        BitMask(0, 0, 16, 16).setBits(
            "0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0," +
            "0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0," +
            "0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0," +
            "0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0," +
            "0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0," +
            "0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
            "0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0," +
            "0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0," +
            "0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0," +
            "0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0," +
            "0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0," +
            "0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0," +
            "0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0")

    fun getTileContactFormType(typeString: String): TileContactFormType {
        return try {
            TileContactFormType.valueOf(typeString.uppercase())
        } catch (e: Exception) {
            if ("rect" == typeString)
                TileContactFormType.RECTANGLE
            else
                TileContactFormType.UNDEF
        }
    }

    fun getTileSizeType(typeString: String): TileSizeType {
        try {
            return TileSizeType.valueOf(typeString.uppercase())
        } catch (e: Exception) {
            if (typeString == "quarterto")
                return TileSizeType.QUARTER_TO
            return TileSizeType.EMPTY
        }
    }

    fun getTileMaterialType(tileMaterial: String): TileMaterialType {
        try {
            return TileMaterialType.valueOf(tileMaterial.uppercase())
        } catch (e: Exception) {
            if (tileMaterial == "terrain")
                return TileMaterialType.TERRAIN_SOLID
            return TileMaterialType.NONE
        }
    }

    fun getTileDirection(direction: String): TileDirection {
        return try {
            TileDirection.valueOf(direction.uppercase())
        } catch (e: Exception) {
            when (direction) {
                "nw", "NW" -> TileDirection.NORTH_WEST
                "n", "N" -> TileDirection.NORTH
                "ne", "NE" -> TileDirection.NORTH_EAST
                "e", "E" -> TileDirection.EAST
                "se", "SE" -> TileDirection.SOUTH_EAST
                "s", "S" -> TileDirection.SOUTH
                "sw", "SW" -> TileDirection.SOUTH_WEST
                "w", "W" -> TileDirection.WEST
                else -> TileDirection.NONE
            }
        }
    }

    fun getTileOrientation(orientation: String): TileOrientation {
        return try {
            TileOrientation.valueOf(orientation.uppercase())
        } catch (e: Exception) {
            when (orientation) {
                "h", "H" -> TileOrientation.HORIZONTAL
                "v", "V" -> TileOrientation.VERTICAL
                else -> TileOrientation.NONE
            }
        }
    }

    fun getColorFromString(stringValue: String): Vector4f? {
        if (StringUtils.isBlank(stringValue))
            return null

        return try {
            val rgbaString = stringValue.split(":")
            Vector4f(
                rgbaString[0].toFloat(),
                rgbaString[1].toFloat(),
                rgbaString[2].toFloat(),
                rgbaString[3].toFloat())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getTileDimensionType(tileWidth: Int): TileDimType {
        return when (tileWidth) {
            8 -> TileDimType.EIGHT
            16 -> TileDimType.SIXTEEN
            32 -> TileDimType.THIRTY_TWO
            else -> TileDimType.SIXTEEN
        }
    }

    private val bitMaskCache: MutableMap<String, BitMask> = HashMap()
    fun createTileContactBitMask(
        formType: TileContactFormType,
        tileSizeType: TileSizeType = TileSizeType.EMPTY,
        tileOrientation: TileOrientation = TileOrientation.NONE,
        tileDirection: TileDirection = TileDirection.NONE,
        dimType: TileDimType = TileDimType.EIGHT): BitMask? {

        val cacheKey = "$formType $tileSizeType $tileOrientation $tileDirection $dimType"
        if (cacheKey in bitMaskCache)
            return bitMaskCache[cacheKey]

        val size = when (dimType) {
            TileDimType.EIGHT -> 8
            TileDimType.SIXTEEN -> 16
            TileDimType.THIRTY_TWO -> 32
        }
        val half = size / 2

        val result: BitMask? = when (formType) {
            TileContactFormType.QUAD, TileContactFormType.RECTANGLE -> when (tileSizeType) {
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.NORTH -> BitMask(0, 0, size, size).setRegion(0, 0, size, half)
                    TileDirection.EAST -> BitMask(0, 0, size, size).setRegion(half, 0, half, size)
                    TileDirection.SOUTH ->  BitMask(0, 0, size, size).setRegion(0, half, size, half)
                    TileDirection.WEST -> BitMask(0, 0, size, size).setRegion(0, 0, half, size)
                    else -> null
                }
                TileSizeType.QUARTER -> when (tileDirection) {
                    TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setRegion(0, 0, half, half)
                    TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setRegion(half, 0, half, half)
                    TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setRegion(half, half, half, half)
                    TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setRegion(0, half, half, half)
                    else -> null
                }
                else -> null
            }
            TileContactFormType.SLOPE -> when (tileSizeType) {
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST)
                    TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                    TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST)
                    TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST)
                    else -> null
                }
                TileSizeType.QUARTER -> when (tileOrientation) {
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    TileOrientation.VERTICAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    else -> null
                }
                TileSizeType.QUARTER_TO -> when (tileOrientation) {
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(
                            Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(
                            Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(
                            Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(
                            Direction.SOUTH_WEST)
                        else -> null
                    }
                    TileOrientation.VERTICAL -> when (tileDirection) {
                        TileDirection.NORTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_WEST)
                        TileDirection.NORTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_EAST)
                        TileDirection.SOUTH_EAST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_EAST)
                        TileDirection.SOUTH_WEST -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_WEST)
                        else -> null
                    }
                    else -> null
                }
                else -> null
            }
            TileContactFormType.CIRCLE -> when (tileSizeType) {
                TileSizeType.FULL -> when (dimType) {
                    TileDimType.EIGHT -> createFullCircleBitMask_8()
                    TileDimType.SIXTEEN -> createFullCircleBitMask_16()
                    else -> null
                }
                else -> null
            }
            TileContactFormType.SPIKE -> when (tileSizeType) {
                TileSizeType.FULL -> when (tileOrientation) {
                    TileOrientation.VERTICAL -> when (tileDirection) {
                        TileDirection.SOUTH -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_EAST)
                            .and(BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.SOUTH_WEST))
                        TileDirection.NORTH -> BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_EAST)
                            .and(BitMask(0, 0, size, size).setVerticalQuarterToSlopeRegion(Direction.NORTH_WEST))
                        else -> null
                    }
                    TileOrientation.HORIZONTAL -> when (tileDirection) {
                        TileDirection.WEST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_WEST)
                            .and(BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_WEST))
                        TileDirection.EAST -> BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.NORTH_EAST)
                            .and(BitMask(0, 0, size, size).setHorizontalQuarterToSlopeRegion(Direction.SOUTH_EAST))
                        else -> null
                    }
                    else -> null
                }
                TileSizeType.HALF -> when (tileDirection) {
                    TileDirection.SOUTH -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST))
                    TileDirection.NORTH -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST))
                    TileDirection.WEST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_WEST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_WEST))
                    TileDirection.EAST -> BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.NORTH_EAST)
                        .and(BitMask(0, 0, size, size).setHalfSlopeRegion(Direction.SOUTH_EAST))
                    else -> null
                }
                else -> null
            }
            TileContactFormType.RHOMBUS -> when(tileSizeType) {
                TileSizeType.FULL -> when (dimType) {
                    TileDimType.EIGHT -> createFullRhombusBitMask_8()
                    TileDimType.SIXTEEN -> createFullRhombusBitMask_16()
                    else -> null
                }
                else -> null
            }

            else -> null
        }

        if (result != null)
            bitMaskCache[cacheKey] = result

        return result
    }

}