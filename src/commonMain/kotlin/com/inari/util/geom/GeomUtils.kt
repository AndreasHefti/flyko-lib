package com.inari.util.geom


import com.inari.util.collection.BitSet
import com.inari.util.geom.Direction.*
import kotlin.jvm.JvmField
import kotlin.math.*

enum class Orientation {
    NONE,
    NORTH,
    EAST,
    SOUTH,
    WEST
}

class CubicBezierCurve(
    @JvmField val p0: Vector2f = Vector2f(),
    @JvmField val p1: Vector2f = Vector2f(),
    @JvmField val p2: Vector2f = Vector2f(),
    @JvmField val p3: Vector2f = Vector2f()
)

class NormalizedTimeRange(
    @JvmField val from: Float = 0f,
    @JvmField val to: Float = 1f,
) {
    fun contains(t: Float): Boolean = t in from..to
}

/** Eight directions, build of four [Orientation], and a NONE constant
 * containing also a horizontal and vertical enum value that divides the
 * Direction into a horizontal and vertical part.
 *
 * Use this if you have a discrete direction with eight different directions,
 * for example, for input, move, or other things.
 */
enum class Direction
    /** Use this to create a new Direction with specified horizontal and vertical Orientation.
     * @param horizontal horizontal [Orientation] of the new Direction
     * @param vertical vertical [Orientation] of the new Direction
     */
    constructor(
        /** The horizontal orientation NONE/EAST/WEST  */
        val horizontal: Orientation,
        /** The vertical orientation NONE/NORTH/SOUTH  */
        val vertical: Orientation) {

        /** No Direction with also Horizontal.NONE and Vertical.NONE  */
        NONE(Orientation.NONE, Orientation.NONE),
        /** North Direction with Horizontal.NONE and Vertical.UP  */
        NORTH(Orientation.NONE, Orientation.NORTH),
        /** North East Direction with Horizontal.RIGHT and Vertical.UP  */
        NORTH_EAST(Orientation.EAST, Orientation.NORTH),
        /** East Direction with Horizontal.RIGHT and Vertical.NONE  */
        EAST(Orientation.EAST, Orientation.NONE),
        /** South East Direction with Horizontal.RIGHT and Vertical.DOWN  */
        SOUTH_EAST(Orientation.EAST, Orientation.SOUTH),
        /** South Direction with Horizontal.NONE and Vertical.DOWN  */
        SOUTH(Orientation.NONE, Orientation.SOUTH),
        /** South West Direction with Horizontal.LEFT and Vertical.DOWN  */
        SOUTH_WEST(Orientation.WEST, Orientation.SOUTH),
        /** West Direction with Horizontal.LEFT and Vertical.NONE  */
        WEST(Orientation.WEST, Orientation.NONE),
        /** North West Direction with Horizontal.LEFT and Vertical.UP  */
        NORTH_WEST(Orientation.WEST, Orientation.NORTH)
}

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

    inline fun magnitude(v: Vector2i) = sqrt((v.v0 * v.v0 + v.v1 * v.v1).toFloat())
    inline fun magnitude(v: Vector4i) = sqrt((v.v0 * v.v0 + v.v1 * v.v1 + v.v2 * v.v2 + v.v3 * v.v3).toFloat())
    inline fun magnitude(v: Vector2f) = sqrt(v.v0 * v.v0 + v.v1 * v.v1)
    inline fun magnitude(v: Vector3f) = sqrt(v.v0 * v.v0 + v.v1 * v.v1 + v.v2 * v.v2 )
    inline fun magnitude(v: Vector4f) = sqrt(v.v0 * v.v0 + v.v1 * v.v1 + v.v2 * v.v2 + v.v3 * v.v3)

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

    /** Circle with circle intersection.
     *  Vector3i contains (x,y,radius)
     */
    inline fun intersectCircle(circle1: Vector3i, circle2: Vector3i): Boolean {
        val dx = (circle1.x + circle1.radius) - (circle2.x + circle2.radius)
        val dy = (circle1.y + circle1.radius) - (circle2.y + circle2.radius)
        return sqrt((dx * dx + dy * dy).toDouble()) < circle1.radius + circle2.radius
    }

    /** Circle with circle intersection.
     *  Vector3f contains (x,y,radius)
     */
    inline fun intersectCircle(circle1: Vector3f, circle2: Vector3f): Boolean {
        val dx = (circle1.x + circle1.radius) - (circle2.x + circle2.radius)
        val dy = (circle1.y + circle1.radius) - (circle2.y + circle2.radius)
        return sqrt((dx * dx + dy * dy).toDouble()) < circle1.radius + circle2.radius
    }

    /** Circle with rectangle intersection.
     *  Vector3i : (x,y,radius)
     *  Vector4i : (x,y,width, height)
     */
    inline fun intersectCircle(circle: Vector3i, rect: Vector4i): Boolean {
        val dx = abs(circle.x - rect.x) - rect.width / 2
        val dy = abs(circle.y - rect.y) - rect.height / 2

        if (dx > circle.radius || dy > circle.radius)
            return false

        return if (dx <= 0 || dy <= 0)
            true
        else
            dx * dx + dy * dy <= circle.radius * circle.radius
    }

    /** Circle with rectangle intersection.
     *  Vector3i : (x,y,radius)
     *  Vector4i : (x,y,width, height)
     */
    inline fun intersectCircle(circle: Vector3f, rect: Vector4f): Boolean {
        val dx = abs(circle.x - rect.x) - rect.width / 2
        val dy = abs(circle.y - rect.y) - rect.height / 2

        if (dx > circle.radius || dy > circle.radius)
            return false

        return if (dx <= 0 || dy <= 0)
            true
        else
            dx * dx + dy * dy <= circle.radius * circle.radius
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

    inline fun intoBoundary(v: Int, lower: Int, upper: Int): Int =
        when {
            (v < lower) -> lower
            (v > upper) -> upper
            else -> v
        }

    inline fun intoBoundary(v: Float, lower: Float, upper: Float): Float =
        when {
            (v < lower) -> lower
            (v > upper) -> upper
            else -> v
        }

    inline fun angleX(v: Vector2f) = atan2(v.y, v.x)
    inline fun angleY(v: Vector2f) = atan2(v.x, v.y)
    inline fun radToDeg(r: Float, invert: Boolean = false) =
        if (invert)
            -r * (180f / PI_F)
        else
            r * (180f / PI_F)

    /**
     * n' = v0+(v1−v0 / r1−r0)(n−r0)
     */
    inline fun transformRange(n: Float, r0: Float = 0f, r1: Float = 1f, v0: Float = 0f, v1: Float = 1f): Float =
        v0 + ((v1 - v0) / (r1 - r0)) * (n - r0)





    private val tmpV0 = Vector2f()
    private val tmpV1 = Vector2f()
    private val tmpV2 = Vector2f()
    private val tmpV3 = Vector2f()
    inline fun bezierCurvePoint(curve: CubicBezierCurve, t: Float, invert: Boolean = false) =
        if (invert)
            bezierCurvePoint(curve.p3, curve.p2, curve.p1, curve.p0, t)
        else
            bezierCurvePoint(curve.p0, curve.p1, curve.p2, curve.p3, t)
    /** u = 1f - t
     *  t2 = t * t
     *  u2 = u * u
     *  u3 = u2 * u
     *  t3 = t2 * t
     *
     *  v(t) = (u3) * v0 + (3f * u2 * t) * v1 + (3f * u * t2) * v2 +(t3) * v3;
     **/
    fun bezierCurvePoint(v0: Vector2f, v1: Vector2f, v2: Vector2f, v3: Vector2f, t: Float): Vector2f {
        val u = 1f - t
        val t2 = t * t
        val u2 = u * u
        val u3 = u2 * u
        val t3 = t2 * t

        return tmpV0(v0) * u3 + tmpV1(v1) * (3f * u2 * t) + tmpV2(v2) * (3f * u * t2) + tmpV3(v3) * t3
    }

    inline fun bezierCurveAngleX(curve: CubicBezierCurve, t: Float, invert: Boolean = false) =
        if (invert)
            bezierCurveAngleX(curve.p3, curve.p2, curve.p1, curve.p0, t)
        else
            bezierCurveAngleX(curve.p0, curve.p1, curve.p2, curve.p3, t)
    /** v′(t)=(1−t)2 (v1−v0) + 2t(1−t) (v2−v1) + t2 (v3−v2)
     *  ax(rad) = atan2(v.y'(t), v.x'(t))
     */
    fun bezierCurveAngleX(v0: Vector2f, v1: Vector2f, v2: Vector2f, v3: Vector2f, t: Float): Float {
        tmpV0(v1) - v0
        tmpV1(v2) - v1
        tmpV2(v3) - v2

        return GeomUtils.angleX(tmpV3(tmpV0) * (1f - t).pow(2f) + tmpV1 * (2f * t * (1f - t)) + tmpV2 * t.pow(2f))
    }
}




