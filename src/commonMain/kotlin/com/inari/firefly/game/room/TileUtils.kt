package com.inari.firefly.game.room

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.graphics.sprite.AccessibleTexture
import com.inari.util.NO_NAME
import com.inari.util.StringUtils
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.geom.BitMask
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

object TileUtils {

    @JvmField val TILE_ASPECT_GROUP = IndexedAspectType("TILE_ASPECT_GROUP")
    @JvmField val UNDEFINED_TILE_ASPECT = TILE_ASPECT_GROUP.createAspect("UNDEFINED_TILE_ASPECT")

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
    fun loadTileContactBitMask(
        bitmaskTextureAssetKey: ComponentKey,
        bounds: Vector4i,
    ) : BitMask? {

        val name = if (bitmaskTextureAssetKey.name != NO_NAME)
            "${bitmaskTextureAssetKey.name}_${bounds.x}_${bounds.y}"
        else
            null

        // get from cache if available
        if (name in bitMaskCache)
            return bitMaskCache[name]

        // load bitmask texture asset to sample bitmask from texture
        val bitmaskTextureAsset = AccessibleTexture[bitmaskTextureAssetKey]
        if (!bitmaskTextureAsset.loaded)
            AccessibleTexture.load(bitmaskTextureAssetKey)

        val bitset = BitMask(0, 0, bounds.width, bounds.height)
        var x = bounds.x * bounds.width
        var y = bounds.y * bounds.height
        var xb = 0
        var yb = 0
        while (yb < bounds.height) {
            while (xb < bounds.width) {
                val d1Pos = (y * bitmaskTextureAsset.width + x) * 4
                val sample = bitmaskTextureAsset.pixels!![d1Pos] +
                        bitmaskTextureAsset.pixels!![d1Pos] +
                        bitmaskTextureAsset.pixels!![d1Pos] +
                        bitmaskTextureAsset.pixels!![d1Pos]
                if (sample != 0)
                    bitset.setBit(xb, yb)
                x++
                xb++
            }
            xb = 0
            x = bounds.x * bounds.width
            y++
            yb++
        }

         // cache the new bitset
        if (name != null)
            bitMaskCache[name] = bitset

        return bitset
    }
}