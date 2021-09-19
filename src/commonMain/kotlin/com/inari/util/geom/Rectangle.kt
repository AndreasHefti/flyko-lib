package com.inari.util.geom

import com.inari.util.StringUtils
import kotlin.jvm.JvmField


/** A simple Rectangle with integer precision
 *  @param width The width of the Rectangle
 *  @param height The height of the Rectangle
 *  */
data class Rectangle constructor(
    @JvmField var pos: Position = Position(0, 0),
    @JvmField var width: Int = 0,
    @JvmField var height: Int = 0
){

    /** Use this to create a Rectangle on specified Position with specified width and height
     *
     * @param x the x-axis Position of the new Rectangle
     * @param y the y-axis Position of the new Rectangle
     * @param width the width of the new Rectangle
     * @param height the height of the new Rectangle
     */
    constructor(x: Int, y: Int, width: Int, height: Int) : this(Position(x, y), width, height)
    constructor(other: Rectangle) : this(other.pos, other.width, other.height)

    var x: Int
        set(value) {pos.x = value}
        get() = pos.x

    var y: Int
        set(value) {pos.y = value}
        get() = pos.y

    /** Use this to get the area value (width * height) form this Rectangle.
     * @return the area value (width * height) form this Rectangle.
     */
    val area: Int get() = width * height

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    operator fun invoke(x: Int, y: Int) =
        pos(x, y)

    operator fun invoke(x: Int = 0, y: Int = 0, w: Int, h: Int) {
        pos(x, y)
        width = w
        height = h
    }

    /** Use this to set the attributes of this Rectangle by another Rectangle.
     * @param other the other Rectangle to get/take the attributes from
     */
    operator fun invoke(other: Rectangle) {
        pos(other.pos)
        width = other.width
        height = other.height
    }

    operator fun invoke(fromString: String) {
        val split = fromString.split(",")
        x = try { split[0].toInt() } catch (e: Exception) { x }
        y = try { split[1].toInt() } catch (e: Exception) { y }
        width = try { split[2].toInt() } catch (e: Exception) { width }
        height = try { split[3].toInt() } catch (e: Exception) { height }
    }

    operator fun set(x: Int, y: Int, other: Rectangle) {
        pos(x, y)
        width = other.width
        height = other.height
    }

    fun clear() {
        pos.x = 0
        pos.y = 0
        width = 0
        height = 0
    }

    fun toJsonString(): String = "$x${StringUtils.VALUE_SEPARATOR}$y" +
            "${StringUtils.VALUE_SEPARATOR}$width${StringUtils.VALUE_SEPARATOR}$height"
    override fun toString(): String =
        "[x=${pos.x}${StringUtils.VALUE_SEPARATOR}y=${pos.y}" +
                "${StringUtils.VALUE_SEPARATOR}width=$width${StringUtils.VALUE_SEPARATOR}height=$height]"
}
