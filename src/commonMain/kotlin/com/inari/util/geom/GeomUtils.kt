package com.inari.util.geom


import com.inari.util.collection.BitSet
import com.inari.util.geom.Direction.*
import kotlin.math.*


// TODO write tests
object GeomUtils {

    const val halfPi = PI / 2.0
    const val tau = 2.0 * PI
    const val PI_F = PI.toFloat()
    const val PI_2_F = PI_F / 2f

    const val NULL_POINT_STRING = "0,0"
    const val NULL_RECTANGLE_STRING = "0,0,0,0"

    const val TOP_SIDE = 1
    const val RIGHT_SIDE = 1 shl 2
    const val BOTTOM_SIDE = 1 shl 3
    const val LEFT_SIDE = 1 shl 4


    inline fun lerp(v0: Int, v1: Int, t: Float): Int = ((1 - t) * v0 + t * v1).toInt()
    inline fun lerp(v0: Float, v1: Float, t: Float): Float = (1 - t) * v0 + t * v1
    inline fun lerp(v0: Vector2i, v1: Vector2i, t: Float, target: Vector2i) {
        target.x = lerp(v0.x, v1.x, t)
        target.y = lerp(v0.y, v1.y, t)
    }
    inline fun lerp(v0: Vector2f, v1: Vector2f, t: Float, target: Vector2f) {
        target.v0 = lerp(v0.v0, v1.v0, t)
        target.v1 = lerp(v0.v1, v1.v1, t)
    }
    inline fun lerp(v0: Vector3f, v1: Vector3f, t: Float, target: Vector3f) {
        target.v0 = lerp(v0.v0, v1.v0, t)
        target.v1 = lerp(v0.v1, v1.v1, t)
        target.v2 = lerp(v0.v2, v1.v2, t)
    }
    inline fun lerp(v0: Vector4f, v1: Vector4f, t: Float, target: Vector4f) {
        target.v0 = lerp(v0.v0, v1.v0, t)
        target.v1 = lerp(v0.v1, v1.v1, t)
        target.v2 = lerp(v0.v2, v1.v2, t)
        target.v3 = lerp(v0.v3, v1.v3, t)
    }

    inline fun sqrtf(value: Float): Float =
        sqrt(value.toDouble()).toFloat()

    inline fun powf(v1: Float, v2: Float): Float =
        v1.toDouble().pow(v2.toDouble()).toFloat()

    inline fun sinf(value: Float): Float =
        sin(value.toDouble()).toFloat()

    inline fun cosf(value: Float): Float =
        cos(value.toDouble()).toFloat()

    inline fun getDistance(p1: Vector2i, p2: Vector2i): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y

        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    inline fun intersect(r1: Vector4i, r2: Vector4i): Boolean {
        val r1Right = r1.x + r1.width
        val r1Bottom = r1.y + r1.height
        val r2Right = r2.x + r2.width
        val r2Bottom = r2.y + r2.height

        return !(r2.x >= r1Right ||
            r2Right <= r1.x ||
            r2.y >= r1Bottom ||
            r2Bottom <= r1.y)
    }

    inline fun getIntersectionCode(r: Vector4i, r1: Vector4i): Int {
        val ax1 = r.x
        val ay1 = r.y
        val ax2 = r.x + r.width - 1
        val ay2 = r.y + r.height - 1

        val bx1 = r1.x
        val by1 = r1.y
        val bx2 = r1.x + r1.width - 1
        val by2 = r1.y + r1.height - 1

        var code = 0

        if (bx2 >= ax1 && bx1 <= ax2) {
            if (by1 > ay1 && by1 <= ay2) {
                code = code or TOP_SIDE
            }
            if (by2 >= ay1 && by2 < ay2) {
                code = code or BOTTOM_SIDE
            }
        }

        if (by2 >= ay1 && by1 <= ay2) {
            if (bx1 > ax1 && bx1 <= ax2) {
                code = code or LEFT_SIDE
            }
            if (bx2 >= ax1 && bx2 < ax2) {
                code = code or RIGHT_SIDE
            }
        }
        return code
    }

    inline fun intersection(r: Vector4i, r1: Vector4i): Vector4i {
        val x1 = max(r.x, r1.x)
        val y1 = max(r.y, r1.y)
        val x2 = min(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = min(r.y + r.height - 1, r1.y + r1.height - 1)
        return Vector4i(x1, y1, max(0, x2 - x1 + 1), max(0, y2 - y1 + 1))
    }

    inline fun intersection(x1: Int, width1: Int, x2: Int, width2: Int): Int {
        return max(0, min(x1 + width1 - 1, x2 + width2 - 1) - max(x1, x2) + 1)
    }

    inline fun intersection(r: Vector4i, r1: Vector4i, result: Vector4i): Vector4i {
        result.x = max(r.x, r1.x)
        result.y = max(r.y, r1.y)
        result.width = max(0, min(r.x + r.width - 1, r1.x + r1.width - 1) - result.x + 1)
        result.height = max(0, min(r.y + r.height - 1, r1.y + r1.height - 1) - result.y + 1)
        return result
    }

    inline fun union(r: Vector4i, r1: Vector4i): Vector4i {
        val x1 = min(r.x, r1.x)
        val y1 = min(r.y, r1.y)
        val x2 = max(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = max(r.y + r.height - 1, r1.y + r1.height - 1)
        return Vector4i(x1, y1, x2 - x1 + 1, y2 - y1 + 1)
    }

    inline fun unionAdd(r: Vector4i, r1: Vector4i) {
        val x1 = min(r.x, r1.x)
        val y1 = min(r.y, r1.y)
        val x2 = max(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = max(r.y + r.height - 1, r1.y + r1.height - 1)
        r.x = x1
        r.y = y1
        r.width = x2 - x1 + 1
        r.height = y2 - y1 + 1
    }

    fun getBoundary(r: Vector4i, side: Int): Int =
        when (side) {
            LEFT_SIDE -> r.x
            TOP_SIDE -> r.y
            RIGHT_SIDE -> r.x + r.width - 1
            BOTTOM_SIDE -> r.y + r.height - 1
            else -> r.x
        }

    inline fun contains(r: Vector4i, x: Int, y: Int): Boolean {
        return x >= r.x &&
            y >= r.y &&
            x < r.x + r.width &&
            y < r.y + r.height
    }

    inline fun contains(r: Vector4i, p: Vector2i): Boolean =
        contains(r, p.x, p.y)

    inline fun contains(r: Vector4i, r1: Vector4i): Boolean =
        r1.x >= r.x &&
        r1.y >= r.y &&
        r1.x + r1.width <= r.x + r.width &&
        r1.y + r1.height <= r.y + r.height

    inline fun getOppositeSide(side: Int): Int =
        when (side) {
            LEFT_SIDE ->  RIGHT_SIDE
            TOP_SIDE ->  BOTTOM_SIDE
            RIGHT_SIDE ->  LEFT_SIDE
            BOTTOM_SIDE ->  TOP_SIDE
            else ->  -1
        }

    inline fun setOutsideBoundary(r: Vector4i, side: Int, boundary: Int): Vector4i =
        when (side) {
            LEFT_SIDE -> {
                r.width += r.x - boundary
                r.x = boundary
                r
            }
            TOP_SIDE -> {
                r.height += r.y - boundary
                r.y = boundary
                r
            }
            RIGHT_SIDE -> {
                r.width = boundary - r.x
                r
            }
            BOTTOM_SIDE -> {
                r.height = boundary - r.y
                r
            }
            else -> {r}
        }

    inline fun rotateRight(d: Direction): Direction =
        when (d) {
            NORTH -> NORTH_EAST
            NORTH_EAST -> EAST
            EAST -> SOUTH_EAST
            SOUTH_EAST -> SOUTH
            SOUTH -> SOUTH_WEST
            SOUTH_WEST -> WEST
            WEST -> NORTH_WEST
            NORTH_WEST -> NORTH
            else -> NONE
        }

    inline fun rotateRight2(d: Direction): Direction =
        when (d) {
            NORTH -> EAST
            NORTH_EAST -> SOUTH_EAST
            EAST -> SOUTH
            SOUTH_EAST -> SOUTH_WEST
            SOUTH -> WEST
            SOUTH_WEST -> NORTH_WEST
            WEST -> NORTH
            NORTH_WEST -> NORTH_EAST
            else -> NONE
        }

    inline fun rotateLeft(d: Direction): Direction =
        when (d) {
            NORTH -> NORTH_WEST
            NORTH_WEST -> WEST
            WEST -> SOUTH_WEST
            SOUTH_WEST -> SOUTH
            SOUTH -> SOUTH_EAST
            SOUTH_EAST -> EAST
            EAST -> NORTH_EAST
            NORTH_EAST -> NORTH
            else -> NONE
        }

    inline fun rotateLeft2(d: Direction): Direction =
        when (d) {
            NORTH -> WEST
            NORTH_WEST -> SOUTH_WEST
            WEST -> SOUTH
            SOUTH_WEST -> SOUTH_EAST
            SOUTH -> EAST
            SOUTH_EAST -> NORTH_EAST
            EAST -> NORTH
            NORTH_EAST -> NORTH_WEST
            else -> NONE
        }

    inline fun isHorizontal(d: Direction): Boolean =
        d == EAST || d == WEST

    inline fun isVertical(d: Direction): Boolean =
        d == NORTH || d == SOUTH

    inline fun translateTo(p: Vector2i, to: Vector2i) {
        p.x = to.x
        p.y = to.y
    }

    inline fun getTranslatedXPos(p: Vector2i, d: Direction, dx: Int = 1): Int =
        when (d.horizontal) {
            Orientation.WEST -> p.x - dx
            Orientation.EAST -> p.x + dx
            else -> p.x
        }

    inline fun getTranslatedYPos(p: Vector2i, d: Direction, dy: Int = 1): Int =
        when (d.vertical) {
            Orientation.SOUTH -> p.y + dy
            Orientation.NORTH -> p.y - dy
            else -> p.y
        }

    inline fun movePosition(position: Vector2i, d: Direction, distance: Int, originUpperCorner: Boolean) {
        movePosition(position, d.horizontal, distance, originUpperCorner)
        movePosition(position, d.vertical, distance, originUpperCorner)
    }

    inline fun bitMaskIntersection(source: BitSet, sourceRect: Vector4i, intersectionRect: Vector4i, result: BitSet) {
        result.clear()
        var y = 0
        var x = 0
        while (y < intersectionRect.height) {
            while (x < intersectionRect.width) {
                result[getFlatArrayIndex(x, y, intersectionRect.width)] =
                    source[(intersectionRect.y + y) * sourceRect.width + intersectionRect.x + x]
                x++
            }
            y++
        }
    }

    inline fun getFlatArrayIndex(x: Int, y: Int, width: Int): Int =
        y * width + x

    inline fun movePosition(pos: Vector2i, orientation: Orientation, distance: Int = 1, originUpperCorner: Boolean = true) =
        when (orientation) {
            Orientation.NORTH -> pos.y = if (originUpperCorner) pos.y - distance else pos.y + distance
            Orientation.SOUTH -> pos.y = if (originUpperCorner) pos.y + distance else pos.y - distance
            Orientation.WEST -> pos.x -= distance
            Orientation.EAST -> pos.x += distance
            else -> {}
        }

    inline fun newVec4f(jsonString: String): Vector4f {
        val result = Vector4f()
        result(jsonString)
        return result
    }

    inline fun hasAlpha(color: Vector4f): Boolean = color.a < 1f

    inline fun rgbA8888(color: Vector4f): Int =
        (color.r * 255).toInt() shl 24 or
        ((color.g * 255).toInt() shl 16) or
        ((color.b * 255).toInt() shl 8) or
        (color.a * 255).toInt()

    inline fun rgB8888(color: Vector4f): Int =
        (color.r * 255).toInt() shl 24 or
        ((color.g * 255).toInt() shl 16) or
        ((color.b * 255).toInt() shl 8) or
        255

    /** Create new Vector4f with specified r/g/b ratio values and no alpha (-1.0f)
     * @param r The red ratio value of the color: 0 - 255
     * @param g The green ratio value of the color: 0 - 255
     * @param b The blue ratio value of the color: 0 - 255
     */
    inline fun colorOf(r: Int, g: Int, b: Int): Vector4f = colorOf(r, g, b, 255)

    /** Create new Vector4f with specified r/g/b/a ratio values
     * @param r The red ratio value of the color: 0 - 255
     * @param g The green ratio value of the color: 0 - 255
     * @param b The blue ratio value of the color: 0 - 255
     * @param a The alpha ratio value of the color: 0 - 255
     */
    inline fun colorOf(r: Int, g: Int, b: Int, a: Int): Vector4f =
        colorOf(r / 255f, g / 255f, b / 255f, a / 255f)

    inline fun colorOf(r: Float, g: Float, b: Float, a: Float) = Vector4f(
        if (r > 1.0f) 1.0f else if (r < 0.0f) 0.0f else r,
        if (g > 1.0f) 1.0f else if (g < 0.0f) 0.0f else g,
        if (b > 1.0f) 1.0f else if (b < 0.0f) 0.0f else b,
        if (a > 1.0f) 1.0f else if (a < 0.0f) 0.0f else a
    )

    // #rrggbbaa
    inline fun colorOf(rgba: String): Vector4f {
        val hexString = if (rgba.startsWith("#"))
            rgba.subSequence(1, rgba.length)
        else rgba

        val ints = hexString
            .chunked(2)
            .map { it.toInt(16) }

        return if (ints.size == 3)
            colorOf(ints[0], ints[1], ints[2])
        else colorOf(ints[0], ints[1], ints[2], ints[3])
    }

    inline fun area(rect: Vector4i) = rect.width * rect.height
}
