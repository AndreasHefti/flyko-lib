package com.inari.util.graphics

import kotlin.jvm.JvmField

interface IColor {
    val r: Float
    val g: Float
    val b: Float
    val a: Float

    val hasAlpha: Boolean get() = a < 1f

    val rgbA8888: Int
        get() = (r * 255).toInt() shl 24 or
                ((g * 255).toInt() shl 16) or
                ((b * 255).toInt() shl 8) or
                (a * 255).toInt()

    val rgB8888: Int
        get() = (r * 255).toInt() shl 24 or
                ((g * 255).toInt() shl 16) or
                ((b * 255).toInt() shl 8) or
                255

    companion object {

        @JvmField val BLACK: IColor = ImmutableColor(0f, 0f, 0f, 1f)
        @JvmField val WHITE: IColor = ImmutableColor(1f, 1f, 1f, 1f)
        @JvmField val RED:  IColor = ImmutableColor(1f, 0f, 0f, 1f)
        @JvmField val GREEN:  IColor = ImmutableColor(0f, 1f, 0f, 1f)
        @JvmField val BLU:  IColor = ImmutableColor(0f, 0f, 1f, 1f)

        /** Create new IColor with specified r/g/b ratio values and no alpha (-1.0f)
         * @param r The red ratio value of the color: 0 - 255
         * @param g The green ratio value of the color: 0 - 255
         * @param b The blue ratio value of the color: 0 - 255
         */
        fun of(r: Int, g: Int, b: Int): IColor {
            return ImmutableColor(r / 255f, g / 255f, b / 255f)
        }

        /** Create new IColor with specified r/g/b/a ratio values
         * @param r The red ratio value of the color: 0 - 255
         * @param g The green ratio value of the color: 0 - 255
         * @param b The blue ratio value of the color: 0 - 255
         * @param a The alpha ratio value of the color: 0 - 255
         */
        fun of(r: Int, g: Int, b: Int, a: Int): IColor {
            return ImmutableColor(r / 255f, g / 255f, b / 255f, a / 255f)
        }

        /** Create new MutableColor with specified r/g/b ratio values and no alpha (-1.0f)
         * @param r The red ratio value of the color: 0 - 255
         * @param g The green ratio value of the color: 0 - 255
         * @param b The blue ratio value of the color: 0 - 255
         */
        fun ofMutable(r: Int, g: Int, b: Int): MutableColor {
            return MutableColor(r / 255f, g / 255f, b / 255f)
        }

        /** Create new MutableColor with specified r/g/b/a ratio values
         * @param r The red ratio value of the color: 0 - 255
         * @param g The green ratio value of the color: 0 - 255
         * @param b The blue ratio value of the color: 0 - 255
         * @param a The alpha ratio value of the color: 0 - 255
         */
        fun ofMutable(r: Int, g: Int, b: Int, a: Int): MutableColor {
            return MutableColor(r / 255f, g / 255f, b / 255f, a / 255f)
        }
    }
}

data class MutableColor(
    var init_r: Float = 0f,
    var init_g: Float = 0f,
    var init_b: Float = 0f,
    var init_a: Float = 1f
) : IColor {
    var r_mutable = adjustValue(init_r)
    var g_mutable = adjustValue(init_g)
    var b_mutable = adjustValue(init_b)
    var a_mutable = adjustValue(init_a)
    override val r: Float
        get() = r_mutable
    override val g: Float
        get() = g_mutable
    override val b: Float
        get() = b_mutable
    override val a: Float
        get() = a_mutable

    operator fun invoke(r: Float = 0f, g: Float = 0f, b: Float = 0f, a: Float = 1f) {
        this.r_mutable = r
        this.g_mutable = g
        this.b_mutable = b
        this.a_mutable = a
    }

    operator fun invoke(color: IColor) {
        this.r_mutable = color.r
        this.g_mutable = color.g
        this.b_mutable = color.b
        this.a_mutable = color.a
    }


    private fun adjustValue(value: Float): Float {
        return when {
            value > 1.0f -> 1.0f
            value < 0.0f -> 0.0f
            else -> value
        }
    }

    override fun toString(): String =
        "[r=$r,g=$g,b=$b,a=$a]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is IColor) return false

        other as IColor

        if (r != other.r) return false
        if (g != other.g) return false
        if (b != other.b) return false
        if (a != other.a) return false
        if (hasAlpha != other.hasAlpha) return false

        return true
    }
}

data class ImmutableColor(
    val init_r: Float = 0f,
    val init_g: Float = 0f,
    val init_b: Float = 0f,
    val init_a: Float = 1f
)  : IColor {
    constructor(other: IColor) : this(other.r, other.g, other.b, other.a)

    override val hasAlpha: Boolean get() = a < 1f
    override val a: Float = adjustValue(init_a)
    override val r: Float = adjustValue(init_r)
    override val g: Float = adjustValue(init_g)
    override val b: Float = adjustValue(init_b)

    private fun adjustValue(value: Float): Float {
        return when {
            value > 1.0f -> 1.0f
            value < 0.0f -> 0.0f
            else -> value
        }
    }

    override fun toString(): String =
        "[r=$r,g=$g,b=$b,a=$a]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is IColor) return false

        other as IColor

        if (r != other.r) return false
        if (g != other.g) return false
        if (b != other.b) return false
        if (a != other.a) return false
        if (hasAlpha != other.hasAlpha) return false

        return true
    }
}

