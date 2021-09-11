package com.inari.util.geom

import kotlin.jvm.JvmField


/** A simple position in a 2D Cartesian coordinate system with float precision.  */
data class PositionF constructor(
    @JvmField var x: Float = 0.0f,
    @JvmField var y: Float = 0.0f
){


    constructor(xi: Int, yi: Int) : this(xi.toFloat(), yi.toFloat())
    constructor(other: PositionF) : this(other.x, other.y)

    val isOrigin: Boolean
        get() = x == 0.0f && y == 0.0f

    operator fun invoke(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    operator fun invoke(x: Int, y: Int) {
        this.x = x.toFloat()
        this.y = y.toFloat()
    }

    /** Use this to set the x/y-axis values from specified Position p
     * @param p the Position to get/take the attributes from
     */
    operator fun invoke(p: Position) {
        x = p.x.toFloat()
        y = p.y.toFloat()
    }

    /** Use this to set the x/y-axis values from specified PositionF p
     * @param p the PositionF to get/take the attributes from
     */
    operator fun invoke(p: PositionF) {
        x = p.x
        y = p.y
    }

    operator fun plus(pos: PositionF): PositionF {
        this.x += pos.x
        this.y += pos.y
        return this
    }

    operator fun plus(offset: Vector2f): PositionF {
        this.x += offset.dx
        this.y += offset.dy
        return this
    }

    operator fun plus(pos: Position): PositionF {
        this.x += pos.x.toFloat()
        this.y += pos.y.toFloat()
        return this
    }

    operator fun minus(pos: PositionF): PositionF {
        this.x -= pos.x
        this.y -= pos.y
        return this
    }

    operator fun minus(pos: Position): PositionF {
        this.x -= pos.x.toFloat()
        this.y -= pos.y.toFloat()
        return this
    }

    operator fun minus(offset: Vector2f): PositionF {
        this.x -= offset.dx
        this.y -= offset.dy
        return this
    }

    override fun toString(): String = "[x=$x,y=$y]"

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
