package com.inari.util.geom

import kotlin.jvm.JvmField

///** A simple one dimensional vector within float precision
// * @param d The vector value
// * */
//data class Vector1f constructor(
//    @JvmField var d: Float = 1.0f
//){
//    operator fun invoke(d: Float) { this.d = d }
//    operator fun invoke(v: Vector1f) { this.d = v.d }
//    operator fun plus(dd: Float): Vector1f {
//        this.d += dd
//        return this
//    }
//    operator fun plus(v: Vector1f): Vector1f {
//        this.d += v.d
//        return this
//    }
//    operator fun minus(dd: Float): Vector1f {
//        this.d -= dd
//        return this
//    }
//    operator fun minus(v: Vector1f): Vector1f {
//        this.d -= v.d
//        return this
//    }
//    operator fun times(dd: Float): Vector1f {
//        this.d *= dd
//        return this
//    }
//    operator fun div(dd: Float): Vector1f {
//        this.d /= dd
//        return this
//    }
//    override fun toString(): String = "[d=$d]"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null) return false
//        if (this::class != other::class) return false
//        other as Vector1f
//        if (d != other.d) return false
//        return true
//    }
//    override fun hashCode(): Int = d.hashCode()
//}
//
///** A simple one dimensional vector within integer precision
// * @param d The vector value
// * */
//data class Vector1i constructor(
//    @JvmField var d: Int = 1
//) {
//    operator fun invoke(d: Int) { this.d = d }
//    operator fun invoke(v: Vector1i) { this.d = v.d }
//    operator fun plus(dd: Int): Vector1i {
//        this.d += dd
//        return this
//    }
//    operator fun plus(v: Vector1i): Vector1i {
//        this.d += v.d
//        return this
//    }
//    operator fun minus(dd: Int): Vector1i {
//        this.d -= dd
//        return this
//    }
//    operator fun minus(v: Vector1i): Vector1i {
//        this.d -= v.d
//        return this
//    }
//    operator fun times(dd: Int): Vector1i {
//        this.d *= dd
//        return this
//    }
//    operator fun div(dd: Int): Vector1i {
//        this.d /= dd
//        return this
//    }
//    override fun toString(): String = "[d=$d]"
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null) return false
//        if (this::class != other::class) return false
//        other as Vector1i
//        if (d != other.d) return false
//        return true
//    }
//    override fun hashCode(): Int = d.hashCode()
//}

interface Vec2f {
    val dx: Float
    val dy: Float
}

class ImmutableVector2f constructor(
    @JvmField override val dx: Float = 1.0f,
    @JvmField override val dy: Float = 1.0f
) : Vec2f {

    operator fun plus(v: Vec2f): ImmutableVector2f =
        ImmutableVector2f(this.dx + v.dx, this.dy + v.dy)

    operator fun minus(v: Vec2f): ImmutableVector2f =
        ImmutableVector2f(this.dx - v.dx, this.dy - v.dy)

    operator fun times(dd: Float): ImmutableVector2f =
        ImmutableVector2f(this.dx * dd, this.dy * dd)

    operator fun times(v: Vec2f): ImmutableVector2f =
        ImmutableVector2f(this.dx * v.dx, this.dy * v.dy)

    operator fun div(dd: Float): ImmutableVector2f =
        ImmutableVector2f(this.dx / dd, this.dy / dd)

    operator fun div(v: Vec2f): ImmutableVector2f =
        ImmutableVector2f(this.dx / v.dx, this.dy / v.dy)

    override fun toString(): String = "[dx=$dx,dy=$dy]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as Vector2f
        if (dx != other.dx && dy != other.dy) return false
        return true
    }
    override fun hashCode(): Int {
        var result = dx.hashCode()
        result = 31 * result + dy.hashCode()
        return result
    }
}

/** A simple two-dimensional (x/y) vector within float precision
 * @param dx the vectors value on x-axis
 * @param dy the vectors value on y-axis
 * */
data class Vector2f constructor(
    @JvmField override var dx: Float = 1.0f,
    @JvmField override var dy: Float = 1.0f
) : Vec2f {

    operator fun invoke(dx: Float, dy: Float): Vector2f {
        this.dx = dx
        this.dy = dy
        return this
    }

    operator fun invoke(v: Vec2f): Vector2f {
        this.dx = v.dx
        this.dy = v.dy
        return this
    }

    operator fun plusAssign(v: Vec2f) {
        this.dx += v.dx
        this.dy += v.dy
    }

    operator fun minusAssign(v: Vec2f) {
        this.dx -= v.dx
        this.dy -= v.dy
    }

    operator fun timesAssign(v: Vec2f) {
        this.dx *= v.dx
        this.dy *= v.dy
    }

    operator fun timesAssign(dd: Float) {
        this.dx *= dd
        this.dy *= dd
    }

    operator fun divAssign(v: Vec2f) {
        this.dx /= v.dx
        this.dy /= v.dy
    }

    operator fun divAssign(dd: Float) {
        this.dx /= dd
        this.dy /= dd
    }

    operator fun plus(v: Vec2f): Vector2f =
        Vector2f(this.dx + v.dx, this.dy + v.dy)

    operator fun minus(v: Vec2f): Vector2f =
        Vector2f(this.dx - v.dx, this.dy - v.dy)

    operator fun times(dd: Float): Vector2f =
        Vector2f(this.dx * dd, this.dy * dd)

    operator fun times(v: Vec2f): Vector2f =
        Vector2f(this.dx * v.dx, this.dy * v.dy)

    operator fun div(dd: Float): Vector2f =
        Vector2f(this.dx / dd, this.dy / dd)

    operator fun div(v: Vec2f): Vector2f =
        Vector2f(this.dx / v.dx, this.dy / v.dy)

    override fun toString(): String = "[dx=$dx,dy=$dy]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as Vector2f
        if (dx != other.dx && dy != other.dy) return false
        return true
    }
    override fun hashCode(): Int {
        var result = dx.hashCode()
        result = 31 * result + dy.hashCode()
        return result
    }
}

interface Vec2i {
    val dx: Int
    val dy: Int
}

data class ImmutableVector2i constructor(
    override val dx: Int = 1,
   override val dy: Int = 1
) : Vec2i {
    operator fun plus(v: Vec2i): ImmutableVector2i =
        ImmutableVector2i(this.dx + v.dx, this.dy + v.dy)

    operator fun minus(v: Vec2i): ImmutableVector2i =
        ImmutableVector2i(this.dx - v.dx, this.dy - v.dy)

    operator fun times(dd: Int): ImmutableVector2i =
        ImmutableVector2i(this.dx * dd, this.dy * dd)

    operator fun times(v: Vec2i): ImmutableVector2i =
        ImmutableVector2i(this.dx * v.dx, this.dy * v.dy)

    operator fun div(dd: Int): ImmutableVector2i =
        ImmutableVector2i(this.dx / dd, this.dy / dd)

    operator fun div(v: Vec2i): ImmutableVector2i =
        ImmutableVector2i(this.dx / v.dx, this.dy / v.dy)

    override fun toString(): String = "[dx=$dx,dy=$dy]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as Vector2i
        if (dx != other.dx && dy != other.dy) return false
        return true
    }
    override fun hashCode(): Int {
        var result = dx
        result = 31 * result + dy
        return result
    }
}

/** A simple two-dimensional (x/y) vector within integer precision
 * @param dx the vectors value on x-axis
 * @param dy the vectors value on y-axis
 * */
data class Vector2i constructor(
    @JvmField override var dx: Int = 1,
    @JvmField override var dy: Int = 1
) : Vec2i {

    operator fun invoke(dx: Int, dy: Int): Vector2i {
        this.dx = dx
        this.dy = dy
        return this
    }

    operator fun invoke(v: Vec2i): Vector2i {
        this.dx = v.dx
        this.dy = v.dy
        return this
    }

    operator fun plusAssign(v: Vec2i) {
        this.dx += v.dx
        this.dy += v.dy
    }

    operator fun minusAssign(v: Vec2i) {
        this.dx -= v.dx
        this.dy -= v.dy
    }

    operator fun timesAssign(v: Vec2i) {
        this.dx *= v.dx
        this.dy *= v.dy
    }

    operator fun timesAssign(dd: Int) {
        this.dx *= dd
        this.dy *= dd
    }

    operator fun divAssign(v: Vec2i) {
        this.dx /= v.dx
        this.dy /= v.dy
    }

    operator fun divAssign(dd: Int) {
        this.dx /= dd
        this.dy /= dd
    }

    operator fun plus(v: Vec2i): Vector2i =
        Vector2i(this.dx + v.dx, this.dy + v.dy)

    operator fun minus(v: Vec2i): Vector2i =
        Vector2i(this.dx - v.dx, this.dy - v.dy)

    operator fun times(dd: Int): Vector2i =
        Vector2i(this.dx * dd, this.dy * dd)

    operator fun times(v: Vec2i): Vector2i =
        Vector2i(this.dx * v.dx, this.dy * v.dy)

    operator fun div(dd: Int): Vector2i =
        Vector2i(this.dx / dd, this.dy / dd)

    operator fun div(v: Vec2i): Vector2i =
        Vector2i(this.dx / v.dx, this.dy / v.dy)

    override fun toString(): String = "[dx=$dx,dy=$dy]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as Vector2i
        if (dx != other.dx && dy != other.dy) return false
        return true
    }
    override fun hashCode(): Int {
        var result = dx
        result = 31 * result + dy
        return result
    }
}