package com.inari.util.geom

import kotlin.jvm.JvmField
import kotlin.math.floor

/** A simple position in a 2D cartesian coordinate system with integer precision.
 * @param x The x-axis value of the position
 * @param y The y-axis value of the position
 * */
data class Position constructor(
    @JvmField var x: Int = 0,
    @JvmField var y: Int = 0
) {
    constructor(other: Position) : this(other.x, other.y)

    operator fun invoke(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    operator fun plus(pos: Position): Position {
        this.x += pos.x
        this.y += pos.y
        return this
    }

    operator fun minus(pos: Position): Position {
        this.x -= pos.x
        this.y -= pos.y
        return this
    }

    /** Use this to set the x/y-axis values from specified Position p
     * @param p the Position to get/take the attributes from
     */
    operator fun invoke(p: Position) {
        x = p.x
        y = p.y
    }

    /** Use this to set the x/y-axis values from specified Position p
     * NOTE: uses Math.floor to get the convert float to integer
     * @param p the Position to get/take the attributes from
     */
    operator fun invoke(p: PositionF) {
        x = floor(p.x.toDouble()).toInt()
        y = floor(p.y.toDouble()).toInt()
    }

    override fun toString(): String = "[x=$x,y=$y]"

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}
