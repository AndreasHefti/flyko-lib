package com.inari.util.geom

import com.inari.util.StringUtils
import com.inari.util.collection.BitSet

class BitMask constructor(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0
){

    private val region = Rectangle()
    private var bits: BitSet

    private val tmpRegion = Rectangle()
    private val intersection = Rectangle()

    private var tmpBits: BitSet

    constructor(region: Rectangle) : this(region.x, region.y, region.width, region.height)

    init {
        region.x = x
        region.y = y
        region.width = width
        region.height = height
        bits = BitSet(region.width + region.height)
        tmpBits = BitSet(region.width + region.height)
    }

    fun region(): Rectangle =
        region
    val isEmpty: Boolean get() =
        bits.isEmpty

    fun fill(): BitMask {
        clearMask()
        for (i in 0 until region.width * region.height) {
            bits.set(i)
        }
        return this
    }

    fun reset(region: Rectangle): BitMask {
        reset(region.x, region.y, region.width, region.height)
        return this
    }

    fun reset(x: Int, y: Int, width: Int, height: Int): BitMask {
        region.x = x
        region.y = y
        region.width = width
        region.height = height
        bits.clear()
        tmpBits.clear()
        return this
    }

    fun clearMask(): BitMask {
        bits.clear()
        tmpBits.clear()
        return this
    }



    fun setBit(index: Int): BitMask {
        bits.set(index)
        return this
    }

    fun setBit(x: Int, y: Int, relativeToOrigin: Boolean): BitMask {
        if (relativeToOrigin) {
            setBit(x - region.x, y - region.y)
        } else {
            setBit(x, y)
        }
        return this
    }

    fun setBit(x: Int, y: Int): BitMask {
        if (x < 0 || x >= region.width || y < 0 || y >= region.height) {
            return this
        }

        bits.set(y * region.width + x)
        return this
    }

    fun setBits(fromString: String): BitMask {
        fromString
            .split(",")
            .forEachIndexed { index, s ->
                if (s == "1") setBit(index)
            }
        return this
    }

    fun resetBit(index: Int): BitMask {
        bits[index] = false
        return this
    }

    fun resetBit(x: Int, y: Int, relativeToOrigin: Boolean): BitMask {
        if (relativeToOrigin) {
            resetBit(x - region.x, y - region.y)
        } else {
            resetBit(x, y)
        }
        return this
    }

    fun resetBit(x: Int, y: Int): BitMask {
        if (x < 0 || x >= region.width || y < 0 || y >= region.height) {
            return this
        }

        bits[y * region.width + x] = false
        return this
    }

    fun getBit(x: Int, y: Int): Boolean =
        bits[y * region.width + x]

    fun moveRegion(x: Int, y: Int): BitMask {
        region.x += x
        region.y += y
        return this
    }

    fun setRegion(region: Rectangle, relativeToOrigin: Boolean): BitMask =
        setRegion(region.x, region.y, region.width, region.height, relativeToOrigin)

    fun setRegion(x: Int, y: Int, width: Int, height: Int): BitMask =
        setRegion(x, y, width, height, true)

    fun setRegion(x: Int, y: Int, width: Int, height: Int, relativeToOrigin: Boolean): BitMask =
        if (relativeToOrigin)
            setIntersectionRegion(x, y, width, height, true)
        else
            setIntersectionRegion(x + region.x, y + region.y, width, height, true)


    fun resetRegion(region: Rectangle, relativeToOrigin: Boolean): BitMask =
        resetRegion(region.x, region.y, region.width, region.height, relativeToOrigin)

    fun resetRegion(x: Int, y: Int, width: Int, height: Int): BitMask =
        resetRegion(x, y, width, height, true)

    fun resetRegion(x: Int, y: Int, width: Int, height: Int, relativeToOrigin: Boolean): BitMask =
        if (relativeToOrigin)
            setIntersectionRegion(x, y, width, height, false)
        else
            setIntersectionRegion(x + region.x, y + region.y, width, height, false)

    fun setHalfSlopeRegion(direction: Direction): BitMask {
        clearMask()
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                when (direction) {
                    Direction.NORTH_EAST -> if (x >= y) setBit(x, y)
                    Direction.SOUTH_WEST -> if (y >= x) setBit(x, y)
                    Direction.SOUTH_EAST -> if (region.height - 1 - y <= x) setBit(x, y)
                    Direction.NORTH_WEST -> if (x < region.height - y) setBit(x, y)
                    else -> {}
                }
            }
        }
        return this
    }

    fun setHorizontalQuarterSlopeRegion(direction: Direction): BitMask {
        clearMask()
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                when (direction) {
                    Direction.NORTH_EAST -> if (x >= y * 2) setBit(x, y)
                    Direction.SOUTH_WEST -> if (y - region.height / 2 >= x / 2) setBit(x, y)
                    Direction.SOUTH_EAST -> if (x / 2 >= region.height-1-y) setBit(x, y)
                    Direction.NORTH_WEST -> if (region.width-1-x >= y * 2) setBit(x, y)
                    else -> {}
                }
            }
        }
        return this
    }

    fun setVerticalQuarterSlopeRegion(direction: Direction): BitMask {
        clearMask()
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                when (direction) {
                    Direction.NORTH_EAST -> if (x >= (y + region.height) / 2) setBit(x, y)
                    Direction.SOUTH_WEST -> if (y - region.height >= (x - region.width / 2) * 2) setBit(x, y)
                    Direction.SOUTH_EAST -> if ((x - 1) * 2 >= region.height-y+region.height / 2) setBit(x, y)
                    Direction.NORTH_WEST -> if (region.width-1-x >= (y + region.height) / 2) setBit(x, y)
                    else -> {}
                }
            }
        }
        return this
    }

    fun setVerticalQuarterToSlopeRegion(direction: Direction): BitMask {
        clearMask()
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                when (direction) {
                    Direction.NORTH_EAST -> if (x >= y / 2) setBit(x, y)
                    Direction.SOUTH_WEST -> if (y + region.height >= x * 2) setBit(x, y)
                    Direction.SOUTH_EAST -> if (x * 2 >= region.height-1-y) setBit(x, y)
                    Direction.NORTH_WEST -> if (region.width-1-x >= y / 2) setBit(x, y)
                    else -> {}
                }
            }
        }
        return this
    }

    fun setHorizontalQuarterToSlopeRegion(direction: Direction): BitMask {
        clearMask()
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                when (direction) {
                    Direction.NORTH_EAST -> if (x >= y * 2 - region.height) setBit(x, y)
                    Direction.SOUTH_WEST -> if (y >= x / 2) setBit(x, y)
                    Direction.SOUTH_EAST -> if (x / 2 >= ((region.height-1) / 2) -y) setBit(x, y)
                    Direction.NORTH_WEST -> if (region.width-1-x >= (y - region.height / 2) * 2) setBit(x, y)
                    else -> {}
                }
            }
        }
        return this
    }


    private fun setIntersectionRegion(xoffset: Int, yoffset: Int, width: Int, height: Int, set: Boolean): BitMask {
        tmpRegion.x = xoffset
        tmpRegion.y = yoffset
        tmpRegion.width = width
        tmpRegion.height = height
        GeomUtils.intersection(region, tmpRegion, intersection)
        if (intersection.area <= 0) {
            return this
        }

        val x1 = intersection.x - region.x
        val y1 = intersection.y - region.y
        val width1 = x1 + intersection.width
        val height1 = y1 + intersection.height

        for (y in y1 until height1) {
            for (x in x1 until width1) {
                bits[y * region.width + x] = set
            }
        }
        return this
    }

    fun and(other: BitMask): BitMask {
        setTmpBits(other, 0, 0)
        bits.and(tmpBits)
        return this
    }

    fun and(other: BitMask, xoffset: Int, yoffset: Int): BitMask {
        setTmpBits(other, xoffset, yoffset)
        bits.and(tmpBits)
        return this
    }

    fun or(other: BitMask): BitMask {
        setTmpBits(other, 0, 0)
        bits.or(tmpBits)
        return this
    }

    fun or(other: BitMask, xoffset: Int, yoffset: Int): BitMask {
        setTmpBits(other, xoffset, yoffset)
        bits.or(tmpBits)
        return this
    }

    private fun setTmpBits(other: BitMask, xoffset: Int, yoffset: Int): BitMask {
        tmpRegion.x = other.region.x + xoffset
        tmpRegion.y = other.region.y + yoffset
        tmpRegion.width = other.region.width
        tmpRegion.height = other.region.height
        GeomUtils.intersection(region, tmpRegion, intersection)
        if (intersection.area <= 0) {
            return this
        }

        tmpBits.clear()

        // adjust intersection to origin
        val x1 = intersection.x - region.x
        val y1 = intersection.y - region.y
        val x2 = if (intersection.x == 0) other.region.width - intersection.width else intersection.x - tmpRegion.x
        val y2 = if (intersection.y == 0) other.region.height - intersection.height else intersection.y - tmpRegion.y

        for (y in 0 until intersection.height) {
            for (x in 0 until intersection.width) {
                tmpBits[(y + y1) * region.width + (x + x1)] = other.bits[(y + y2) * other.region.width + (x + x2)]
            }
        }
        return this
    }

    fun hasIntersection(region: Rectangle): Boolean {
        GeomUtils.intersection(this.region, region, intersection)
        if (intersection.area <= 0) {
            return false
        }

        val x1 = intersection.x - this.region.x
        val y1 = intersection.y - this.region.y
        val width1 = x1 + intersection.width
        val height1 = y1 + intersection.height

        for (y in y1 until height1) {
            for (x in x1 until width1) {
                if (bits[y * this.region.width + x]) {
                    return true
                }
            }
        }

        return false
    }

    override fun toString(): String =
        "BitMask [region=$region, bits=\n" + StringUtils.bitSetToString(bits, region.width, region.height) + "]"

    companion object {

        fun createIntersectionMask(region: Rectangle, bitmask: BitMask, result: BitMask, xoffset: Int, yoffset: Int, adjustResult: Boolean): Boolean {
            bitmask.region.x += xoffset
            bitmask.region.y += yoffset

            val intersection = createIntersectionMask(bitmask, region, result)

            bitmask.region.x -= xoffset
            bitmask.region.y -= yoffset

            if (adjustResult) {
                result.region.x -= region.x
                result.region.y -= region.y
            }

            return intersection
        }

        fun createIntersectionMask(bitmask: BitMask, region: Rectangle, result: BitMask, xoffset: Int, yoffset: Int, adjustResult: Boolean): Boolean {
            result.tmpRegion.x = region.x + xoffset
            result.tmpRegion.y = region.y + yoffset
            result.tmpRegion.width = region.width
            result.tmpRegion.height = region.height

            val intersection = createIntersectionMask(bitmask, result.tmpRegion, result)

            if (adjustResult) {
                result.region.x -= bitmask.region.x
                result.region.y -= bitmask.region.y
            }

            return intersection
        }

        fun createIntersectionMask(bitmask: BitMask, region: Rectangle, result: BitMask, adjustResult: Boolean): Boolean {
            val intersection = createIntersectionMask(bitmask, region, result)

            if (adjustResult) {
                result.region.x -= bitmask.region.x
                result.region.y -= bitmask.region.y
            }

            return intersection
        }

        fun createIntersectionMask(region: Rectangle, bitmask: BitMask, result: BitMask, adjustResult: Boolean): Boolean {
            val intersection = createIntersectionMask(bitmask, region, result)

            if (adjustResult) {
                result.region.x -= region.x
                result.region.y -= region.y
            }

            return intersection
        }

        fun createIntersectionMask(bitmask: BitMask, region: Rectangle, result: BitMask): Boolean {
            result.clearMask()

            GeomUtils.intersection(bitmask.region, region, result.region)
            if (result.region.area <= 0) {
                return false
            }

            val x1 = result.region.x - bitmask.region.x
            val y1 = result.region.y - bitmask.region.y

            for (y in 0 until result.region.height) {
                for (x in 0 until result.region.width) {
                    result.bits.set(
                        y * result.region.width + x,
                        bitmask.bits.get((y + y1) * bitmask.region.width + (x + x1))
                    )
                }
            }

            return !result.bits.isEmpty
        }

        fun createIntersectionMask(bitmask1: BitMask, bitmask2: BitMask, result: BitMask, xoffset: Int, yoffset: Int, adjustResult: Boolean): Boolean {
            bitmask2.region.x += xoffset
            bitmask2.region.y += yoffset

            val intersection = createIntersectionMask(bitmask1, bitmask2, result)

            bitmask2.region.x -= xoffset
            bitmask2.region.y -= yoffset

            if (adjustResult) {
                result.region.x -= bitmask1.region.x
                result.region.y -= bitmask1.region.y
            }

            return intersection
        }

        fun createIntersectionMask(bitmask1: BitMask, bitmask2: BitMask, result: BitMask, adjustResult: Boolean): Boolean {
            val intersection = createIntersectionMask(bitmask1, bitmask2, result)

            if (adjustResult) {
                result.region.x -= bitmask1.region.x
                result.region.y -= bitmask1.region.y
            }

            return intersection
        }

        fun createIntersectionMask(bitmask1: BitMask, bitmask2: BitMask, result: BitMask, xoffset: Int, yoffset: Int): Boolean {
            bitmask2.region.x += xoffset
            bitmask2.region.y += yoffset

            val intersection = createIntersectionMask(bitmask1, bitmask2, result)

            bitmask2.region.x -= xoffset
            bitmask2.region.y -= yoffset

            return intersection
        }

        fun createIntersectionMask(bitmask1: BitMask, bitmask2: BitMask, result: BitMask): Boolean {
            result.clearMask()


            GeomUtils.intersection(bitmask1.region, bitmask2.region, result.region)
            if (result.region.area <= 0) {
                return false
            }

            val x1 = result.region.x - bitmask1.region.x
            val y1 = result.region.y - bitmask1.region.y
            val x2 = result.region.x - bitmask2.region.x
            val y2 = result.region.y - bitmask2.region.y

            for (y in 0 until result.region.height) {
                for (x in 0 until result.region.width) {
                    result.bits[y * result.region.width + x] =
                        bitmask1.bits[(y + y1) * bitmask1.region.width + (x + x1)] &&
                                bitmask2.bits[(y + y2) * bitmask2.region.width + (x + x2)]
                }
            }

            return !result.bits.isEmpty
        }
    }
}
