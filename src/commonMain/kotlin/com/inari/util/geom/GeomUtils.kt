package com.inari.util.geom


import com.inari.util.collection.BitSet
import com.inari.util.geom.Direction.*
import com.inari.util.graphics.IColor
import com.inari.util.graphics.MutableColor
import kotlin.math.*


// TODO write tests
object GeomUtils {

    const val PI_F = PI.toFloat()
    const val PI_2_F = PI_F / 2f

    const val NULL_POINT_STRING = "0,0"
    const val NULL_RECTANGLE_STRING = "0,0,0,0"

    const val TOP_SIDE = 1
    const val RIGHT_SIDE = 1 shl 2
    const val BOTTOM_SIDE = 1 shl 3
    const val LEFT_SIDE = 1 shl 4

    fun lerp(v0: Int, v1: Int, t: Float): Int = ((1 - t) * v0 + t * v1).toInt()
    fun lerp(v0: Float, v1: Float, t: Float): Float = (1 - t) * v0 + t * v1
    fun lerp(v0: Position, v1: Position, t: Float, target: Position) {
        target.x = lerp(v0.x, v1.x, t)
        target.y = lerp(v0.y, v1.y, t)
    }
    fun lerp(v0: PositionF, v1: PositionF, t: Float, target: PositionF) {
        target.x = lerp(v0.x, v1.x, t)
        target.y = lerp(v0.y, v1.y, t)
    }
    fun lerp(v0: Vec2f, v1: Vec2f, t: Float, target: Vector2f) {
        target.dx = lerp(v0.dx, v1.dx, t)
        target.dy = lerp(v0.dy, v1.dy, t)
    }
    fun lerp(v0: IColor, v1: IColor, t: Float, target: MutableColor) {
        target.r_mutable = lerp(v0.r, v1.r, t)
        target.g_mutable = lerp(v0.g, v1.g, t)
        target.b_mutable = lerp(v0.b, v1.b, t)
        target.a_mutable = lerp(v0.a, v1.a, t)
    }

    fun sqrtf(value: Float): Float =
        sqrt(value.toDouble()).toFloat()

    fun powf(v1: Float, v2: Float): Float =
        v1.toDouble().pow(v2.toDouble()).toFloat()

    fun sinf(value: Float): Float =
        sin(value.toDouble()).toFloat()

    fun cosf(value: Float): Float =
        cos(value.toDouble()).toFloat()

    fun getDistance(p1: Position, p2: Position): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y

        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun intersect(r1: Rectangle, r2: Rectangle): Boolean {
        val r1Right = r1.x + r1.width
        val r1Bottom = r1.y + r1.height
        val r2Right = r2.x + r2.width
        val r2Bottom = r2.y + r2.height

        return !(r2.x >= r1Right ||
            r2Right <= r1.x ||
            r2.y >= r1Bottom ||
            r2Bottom <= r1.y)
    }

    fun getIntersectionCode(r: Rectangle, r1: Rectangle): Int {
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

    fun intersection(r: Rectangle, r1: Rectangle): Rectangle {
        val x1 = max(r.x, r1.x)
        val y1 = max(r.y, r1.y)
        val x2 = min(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = min(r.y + r.height - 1, r1.y + r1.height - 1)
        return Rectangle(x1, y1, max(0, x2 - x1 + 1), max(0, y2 - y1 + 1))
    }

    fun intersection(x1: Int, width1: Int, x2: Int, width2: Int): Int {
        return max(0, min(x1 + width1 - 1, x2 + width2 - 1) - max(x1, x2) + 1)
    }

    fun intersection(r: Rectangle, r1: Rectangle, result: Rectangle): Rectangle {
        result.x = max(r.x, r1.x)
        result.y = max(r.y, r1.y)
        result.width = max(0, min(r.x + r.width - 1, r1.x + r1.width - 1) - result.x + 1)
        result.height = max(0, min(r.y + r.height - 1, r1.y + r1.height - 1) - result.y + 1)
        return result
    }

    fun union(r: Rectangle, r1: Rectangle): Rectangle {
        val x1 = min(r.x, r1.x)
        val y1 = min(r.y, r1.y)
        val x2 = max(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = max(r.y + r.height - 1, r1.y + r1.height - 1)
        return Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1)
    }

    fun unionAdd(r: Rectangle, r1: Rectangle) {
        val x1 = min(r.x, r1.x)
        val y1 = min(r.y, r1.y)
        val x2 = max(r.x + r.width - 1, r1.x + r1.width - 1)
        val y2 = max(r.y + r.height - 1, r1.y + r1.height - 1)
        r.x = x1
        r.y = y1
        r.width = x2 - x1 + 1
        r.height = y2 - y1 + 1
    }

    fun getBoundary(r: Rectangle, side: Int): Int =
        when (side) {
            LEFT_SIDE -> r.x
            TOP_SIDE -> r.y
            RIGHT_SIDE -> r.x + r.width - 1
            BOTTOM_SIDE -> r.y + r.height - 1
            else -> r.x
        }

    fun contains(r: Rectangle, x: Int, y: Int): Boolean {
        return x >= r.x &&
            y >= r.y &&
            x < r.x + r.width &&
            y < r.y + r.height
    }

    fun contains(r: Rectangle, p: Position): Boolean =
        contains(r, p.x, p.y)

    fun contains(r: Rectangle, r1: Rectangle): Boolean =
        r1.x >= r.x &&
        r1.y >= r.y &&
        r1.x + r1.width <= r.x + r.width &&
        r1.y + r1.height <= r.y + r.height

    fun getOppositeSide(side: Int): Int =
        when (side) {
            LEFT_SIDE ->  RIGHT_SIDE
            TOP_SIDE ->  BOTTOM_SIDE
            RIGHT_SIDE ->  LEFT_SIDE
            BOTTOM_SIDE ->  TOP_SIDE
            else ->  -1
        }

    fun setOutsideBoundary(r: Rectangle, side: Int, boundary: Int): Rectangle =
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



    fun rotateRight(d: Direction): Direction =
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

    fun rotateRight2(d: Direction): Direction =
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

    fun rotateLeft(d: Direction): Direction =
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

    fun rotateLeft2(d: Direction): Direction =
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

    fun isHorizontal(d: Direction): Boolean =
        d == EAST || d == WEST

    fun isVertical(d: Direction): Boolean =
        d == NORTH || d == SOUTH

    fun translateTo(p: Position, to: Position) {
        p.x = to.x
        p.y = to.y
    }

    fun translate(p: Position, d: Vector2i) {
        p.x += d.dx
        p.y += d.dy
    }

    fun getTranslatedXPos(p: Position, d: Direction, dx: Int = 1): Int =
        when (d.horizontal) {
            Orientation.WEST -> p.x - dx
            Orientation.EAST -> p.x + dx
            else -> p.x
        }

    fun getTranslatedYPos(p: Position, d: Direction, dy: Int = 1): Int =
        when (d.vertical) {
            Orientation.SOUTH -> p.y + dy
            Orientation.NORTH -> p.y - dy
            else -> p.y
        }

    fun movePosition(position: Position, d: Direction, distance: Int, originUpperCorner: Boolean) {
        movePosition(position, d.horizontal, distance, originUpperCorner)
        movePosition(position, d.vertical, distance, originUpperCorner)
    }

    fun bitMaskIntersection(source: BitSet, sourceRect: Rectangle, intersectionRect: Rectangle, result: BitSet) {
        result.clear()
        var y = 0
        var x = 0
        while (y < intersectionRect.height) {
            while (x < intersectionRect.width) {
                result.set(
                    getFlatArrayIndex(x, y, intersectionRect.width),
                    source.get((intersectionRect.y + y) * sourceRect.width + intersectionRect.x + x)
                )
                x++
            }
            y++
        }
    }

    fun getFlatArrayIndex(x: Int, y: Int, width: Int): Int =
        y * width + x

    fun movePosition(pos: Position, orientation: Orientation, distance: Int = 1, originUpperCorner: Boolean = true) =
        when (orientation) {
            Orientation.NORTH -> pos.y = if (originUpperCorner) pos.y - distance else pos.y + distance
            Orientation.SOUTH -> pos.y = if (originUpperCorner) pos.y + distance else pos.y - distance
            Orientation.WEST -> pos.x -= distance
            Orientation.EAST -> pos.x += distance
            else -> {}
        }
}
