package com.inari.firefly.game.tile

import com.inari.util.geom.BitMask
import com.inari.util.geom.Vector4f

object TileUtils {

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
            TileContactFormType.valueOf(typeString.toUpperCase())
        } catch (e: Exception) {
            if ("rect" == typeString)
                TileContactFormType.RECTANGLE
            else
                TileContactFormType.UNDEF
        }
    }

    fun getTileSizeType(typeString: String): TileSizeType {
        try {
            return TileSizeType.valueOf(typeString.toUpperCase())
        } catch (e: Exception) {
            if (typeString == "quarterto")
                return TileSizeType.QUARTER_TO
            return TileSizeType.EMPTY
        }
    }

    fun getTileMaterialType(tileMaterial: String): TileMaterialType {
        try {
            return TileMaterialType.valueOf(tileMaterial.toUpperCase())
        } catch (e: Exception) {
            if (tileMaterial == "terrain")
                return TileMaterialType.TERRAIN_SOLID
            return TileMaterialType.NONE
        }
    }

    fun getTileDirection(direction: String): TileDirection {
        return try {
            TileDirection.valueOf(direction.toUpperCase())
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
            TileOrientation.valueOf(orientation.toUpperCase())
        } catch (e: Exception) {
            when (orientation) {
                "h", "H" -> TileOrientation.HORIZONTAL
                "v", "V" -> TileOrientation.VERTICAL
                else -> TileOrientation.NONE
            }
        }
    }

    fun getColorFromString(stringValue: String): Vector4f? {
        return try {
            val rgbaString = stringValue.split(":")
            Vector4f(
                rgbaString[0].toFloat(),
                rgbaString[1].toFloat(),
                rgbaString[2].toFloat(),
                rgbaString[3].toFloat(),)
        } catch (e: Exception) {
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

}