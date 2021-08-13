package com.inari.util.geom

interface Vec2f {
    val dx: Float
    val dy: Float
}

class ImmutableVector2f constructor(
    override val dx: Float = 1.0f,
    override val dy: Float = 1.0f
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
    override var dx: Float = 1.0f,
    override var dy: Float = 1.0f
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

    operator fun plus(v: Vec2f) {
        this.dx += v.dx
        this.dy += v.dy
    }

    operator fun minus(v: Vec2f) {
        this.dx -= v.dx
        this.dy -= v.dy
    }

    operator fun times(v: Vec2f) {
        this.dx *= v.dx
        this.dy *= v.dy
    }

    operator fun times(dd: Float) {
        this.dx *= dd
        this.dy *= dd
    }

    operator fun div(v: Vec2f) {
        this.dx /= v.dx
        this.dy /= v.dy
    }

    operator fun div(dd: Float) {
        this.dx /= dd
        this.dy /= dd
    }

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
    override var dx: Int = 1,
    override var dy: Int = 1
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

    operator fun plus(v: Vec2i) {
        this.dx += v.dx
        this.dy += v.dy
    }

    operator fun minus(v: Vec2i) {
        this.dx -= v.dx
        this.dy -= v.dy
    }

    operator fun times(v: Vec2i) {
        this.dx *= v.dx
        this.dy *= v.dy
    }

    operator fun times(dd: Int) {
        this.dx *= dd
        this.dy *= dd
    }

    operator fun div(v: Vec2i) {
        this.dx /= v.dx
        this.dy /= v.dy
    }

    operator fun div(dd: Int) {
        this.dx /= dd
        this.dy /= dd
    }

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