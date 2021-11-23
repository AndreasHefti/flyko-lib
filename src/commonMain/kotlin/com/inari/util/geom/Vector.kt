package com.inari.util.geom

import com.inari.util.StringUtils
import kotlin.jvm.JvmField

class ImmutableVector2i constructor(
    @JvmField inline val v0: Int = 0,
    @JvmField inline val v1: Int = 0
) {
    fun instance(): Vector2i = Vector2i(v0, v1)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImmutableVector2i
        if (v0 != other.v0) return false
        if (v1 != other.v1) return false
        return true
    }
    override fun hashCode(): Int {
        var result = v0
        result = 31 * result + v1
        return result
    }
}
open class Vector2i constructor(
    @JvmField inline var v0: Int = 0,
    @JvmField inline var v1: Int = 0
) {

    constructor(v0: Float, v1: Float) : this(v0.toInt(), v1.toInt())
    constructor(other: Vector2i) : this(other.v0, other.v1)

    inline var x: Int
        get() = v0
        set(value) { v0 = value }
    inline var y: Int
        get() = v1
        set(value) { v1 = value }

    operator fun invoke(v0: Int, v1: Int) {
        this.v0 = v0
        this.v1 = v1
    }

    operator fun invoke(v0: Float, v1: Float) {
        this.v0 = v0.toInt()
        this.v1 = v1.toInt()
    }

    operator fun invoke(v: Vector2i) {
        this.v0 = v.v0
        this.v1 = v.v1
    }

    operator fun invoke(v: ImmutableVector2i) {
        this.v0 = v.v0
        this.v1 = v.v1
    }

    operator fun invoke(v: Vector2f) {
        this.v0 = v.v0.toInt()
        this.v1 = v.v1.toInt()
    }

    open operator fun invoke(jsonString: String) {
        val split = jsonString.split(StringUtils.VALUE_SEPARATOR)
        v0 = try { split[0].toInt() } catch (e: Exception) { v0 }
        v1 = try { split[1].toInt() } catch (e: Exception) { v1 }
    }

    operator fun plus(v: Vector2i) {
        this.v0 += v.v0
        this.v1 += v.v1
    }

    operator fun minus(v: Vector2i) {
        this.v0 -= v.v0
        this.v1 -= v.v1
    }

    operator fun times(v: Vector2i) {
        this.v0 *= v.v0
        this.v1 *= v.v1
    }

    open operator fun times(dd: Int) {
        this.v0 *= dd
        this.v1 *= dd
    }

    operator fun div(v: Vector2i) {
        this.v0 /= v.v0
        this.v1 /= v.v1
    }

    open operator fun div(dd: Int) {
        this.v0 /= dd
        this.v1 /= dd
    }

    open fun toJsonString(): String = "$v0${StringUtils.VALUE_SEPARATOR}$v1"
    override fun toString(): String = "[x=$v0${StringUtils.VALUE_SEPARATOR}y=$v1]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Vector2i
        if (v0 != other.v0) return false
        if (v1 != other.v1) return false
        return true
    }

    override fun hashCode(): Int {
        var result = v0
        result = 31 * result + v1
        return result
    }
}

class ImmutableVector4i constructor(
    @JvmField inline val v0: Int = 0,
    @JvmField inline val v1: Int = 0,
    @JvmField inline val v2: Int = 0,
    @JvmField inline val v3: Int = 0
) {
    fun instance(): Vector4i = Vector4i(v0, v1, v2, v3)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImmutableVector4i
        if (v0 != other.v0) return false
        if (v1 != other.v1) return false
        if (v2 != other.v2) return false
        if (v3 != other.v3) return false
        return true
    }

    override fun hashCode(): Int {
        var result = v0
        result = 31 * result + v1
        result = 31 * result + v2
        result = 31 * result + v3
        return result
    }
}
open class Vector4i constructor(
    v0: Int = 0,
    v1: Int = 0,
    @JvmField inline var v2: Int = 0,
    @JvmField inline var v3: Int = 0
) : Vector2i(v0, v1) {

    constructor(v0: Float, v1: Float, v2: Float, v3: Float) : this(v0.toInt(), v1.toInt(), v2.toInt(), v3.toInt())
    constructor(other: Vector4i) : this(other.v0, other.v1, other.v2, other.v3)
    constructor(other: Vector2i) : this(other.v0, other.v1)

    inline var width: Int
        get() = v2
        set(value) { v2 = value }
    inline var height: Int
        get() = v3
        set(value) { v3 = value }

    operator fun invoke(v0: Int, v1: Int, v2: Int, v3: Int) {
        super.invoke(v0, v1)
        this.v2 = v2
        this.v3 = v3
    }

    operator fun invoke(v0: Float, v1: Float, v2: Float, v3: Float) {
        super.invoke(v0, v1)
        this.v2 = v2.toInt()
        this.v3 = v3.toInt()
    }

    operator fun invoke(v: Vector4i) {
        super.invoke(v)
        this.v2 = v.v2
        this.v3 = v.v3
    }

    operator fun invoke(v: ImmutableVector4i) {
        super.invoke(v.v0, v.v1)
        this.v2 = v.v2
        this.v3 = v.v3
    }

    operator fun invoke(v: Vector4f) {
        super.invoke(v)
        this.v2 = v.v2.toInt()
        this.v3 = v.v3.toInt()
    }

    override operator fun invoke(jsonString: String) {
        val split = jsonString.split(StringUtils.VALUE_SEPARATOR)
        v0 = try { split[0].toInt() } catch (e: Exception) { v0 }
        v1 = try { split[1].toInt() } catch (e: Exception) { v1 }
        v2 = try { split[2].toInt() } catch (e: Exception) { v2 }
        v3 = try { split[3].toInt() } catch (e: Exception) { v3 }
    }

    operator fun plus(v: Vector4i) {
        super.plus(v)
        this.v2 += v.v2
        this.v3 += v.v3
    }

    operator fun minus(v: Vector4i) {
        super.minus(v)
        this.v2 -= v.v2
        this.v3 -= v.v3
    }

    operator fun times(v: Vector4i) {
        super.times(v)
        this.v2 *= v.v2
        this.v3 *= v.v3
    }

    override operator fun times(dd: Int) {
        super.times(dd)
        this.v2 *= dd
        this.v3 *= dd
    }

    operator fun div(v: Vector4i) {
        super.div(v)
        this.v2 /= v.v2
        this.v3 /= v.v3
    }

    override operator fun div(dd: Int) {
        super.div(dd)
        this.v2 /= dd
        this.v3 /= dd
    }

    override fun toJsonString(): String = "$v0${StringUtils.VALUE_SEPARATOR}$v1${StringUtils.VALUE_SEPARATOR}$v2${StringUtils.VALUE_SEPARATOR}$v3"
    override fun toString(): String = "[x=$v0${StringUtils.VALUE_SEPARATOR}y=$v1${StringUtils.VALUE_SEPARATOR}width=$v2${StringUtils.VALUE_SEPARATOR}height=$v3]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false
        other as Vector4i
        if (v2 != other.v2) return false
        if (v3 != other.v3) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + v2
        result = 31 * result + v3
        return result
    }
}

class ImmutableVector2f constructor(
    @JvmField inline val v0: Float = 0.0f,
    @JvmField inline val v1: Float = 0.0f
) {
    inline val x: Float get() = v0
    inline val y: Float get() = v1
    inline val r: Float get() = v0
    inline val g: Float get() = v1
    fun instance(): Vector2f = Vector2f(v0, v1)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImmutableVector2f
        if (v0 != other.v0) return false
        if (v1 != other.v1) return false
        return true
    }

    override fun hashCode(): Int {
        var result = v0.hashCode()
        result = 31 * result + v1.hashCode()
        return result
    }
}
open class Vector2f constructor(
    @JvmField inline var v0: Float = 0.0f,
    @JvmField inline var v1: Float = 0.0f
) {

    constructor(v0: Int, v1: Int) : this(v0.toFloat(), v1.toFloat())
    constructor(other: Vector2f) : this(other.v0, other.v1)

    inline var x: Float
        get() = v0
        set(value) { v0 = value }
    inline var y: Float
        get() = v1
        set(value) { v1 = value }
    inline var r: Float
        get() = v0
        set(value) { v0 = value }
    inline var g: Float
        get() = v1
        set(value) { v1 = value }

    operator fun invoke(v0: Float, v1: Float): Vector2f {
        this.v0 = v0
        this.v1 = v1
        return this
    }

    operator fun invoke(v0: Int, v1: Int): Vector2f {
        this.v0 = v0.toFloat()
        this.v1 = v1.toFloat()
        return this
    }

    operator fun invoke(v: Vector2f) {
        this.v0 = v.v0
        this.v1 = v.v1
    }

    operator fun invoke(v: ImmutableVector2f) {
        this.v0 = v.v0
        this.v1 = v.v1
    }

    operator fun invoke(v: Vector2i) {
        this.v0 = v.v0.toFloat()
        this.v1 = v.v1.toFloat()
    }

    open operator fun invoke(jsonString: String) {
        val split = jsonString.split(StringUtils.VALUE_SEPARATOR)
        v0 = try { split[0].toFloat() } catch (e: Exception) { v0 }
        v1 = try { split[1].toFloat() } catch (e: Exception) { v1 }
    }

    operator fun plus(v: Vector2f) {
        this.v0 += v.v0
        this.v1 += v.v1
    }

    operator fun minus(v: Vector2f) {
        this.v0 -= v.v0
        this.v1 -= v.v1
    }

    operator fun times(v: Vector2f) {
        this.v0 *= v.v0
        this.v1 *= v.v1
    }

    open operator fun times(dd: Float) {
        this.v0 *= dd
        this.v1 *= dd
    }

    operator fun div(v: Vector2f) {
        this.v0 /= v.v0
        this.v1 /= v.v1
    }

    open operator fun div(dd: Float) {
        this.v0 /= dd
        this.v1 /= dd
    }

    open fun toJsonString(): String = "$v0${StringUtils.VALUE_SEPARATOR}$v1"
    override fun toString(): String = "[x=$v0${StringUtils.VALUE_SEPARATOR}y=$v1]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false
        other as Vector2f
        if (v0 != other.v0 && v1 != other.v1) return false
        return true
    }
    override fun hashCode(): Int {
        var result = v0.hashCode()
        result = 31 * result + v1.hashCode()
        return result
    }
}

class ImmutableVector3f constructor(
    @JvmField inline var v0: Float = 0.0f,
    @JvmField inline var v1: Float = 0.0f,
    @JvmField inline var v2: Float = 0.0f
) {
    inline val x: Float get() = v0
    inline val y: Float get() = v1
    inline val r: Float get() = v0
    inline val g: Float get() = v1
    inline val z: Float get() = v2
    inline val b: Float get() = v2
    fun instance(): Vector3f = Vector3f(v0, v1, v2)
}
open class Vector3f constructor(
    v0: Float = 0.0f,
    v1: Float = 0.0f,
    @JvmField inline var v2: Float = 0.0f
) : Vector2f(v0, v1) {

    constructor(v0: Int = 1, v1: Int = 1, v2: Int = 1) : this(v0.toFloat(), v1.toFloat(), v2.toFloat())
    constructor(other: Vector3f) : this(other.v0, other.v1, other.v2)

    inline var z: Float
        get() = v2
        set(value) { v2 = value }
    inline var b: Float
        get() = v2
        set(value) { v2 = value }

    operator fun invoke(v0: Float, v1: Float, v2: Float) {
        super.invoke(v0, v1)
        this.v2 = v2
    }

    operator fun invoke(v: Vector3f) {
        super.invoke(v)
        this.v2 = v.v2
    }

    override operator fun invoke(jsonString: String) {
        val split = jsonString.split(StringUtils.VALUE_SEPARATOR)
        v0 = try { split[0].toFloat() } catch (e: Exception) { v0 }
        v1 = try { split[1].toFloat() } catch (e: Exception) { v1 }
        v2 = try { split[2].toFloat() } catch (e: Exception) { v2 }
    }

    operator fun plus(v: Vector3f) {
        super.invoke(v)
        this.v2 += v.v2
    }

    operator fun minus(v: Vector3f) {
        super.invoke(v)
        this.v2 -= v.v2
    }

    operator fun times(v: Vector3f) {
        super.invoke(v)
        this.v2 *= v.v2
    }

    override operator fun times(dd: Float) {
        super.times(dd)
        this.v2 *= dd
    }

    operator fun div(v: Vector3f) {
        super.div(v)
        this.v2 /= v.v2
    }

    override operator fun div(dd: Float) {
        super.div(dd)
        this.v2 /= dd
    }

    override fun toJsonString(): String = "$v0${StringUtils.VALUE_SEPARATOR}$v1${StringUtils.VALUE_SEPARATOR}$v2"
    override fun toString(): String = "[x=$v0${StringUtils.VALUE_SEPARATOR}y=$v1${StringUtils.VALUE_SEPARATOR}z=$v2]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as Vector3f

        if (v2 != other.v2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + v2.hashCode()
        return result
    }
}

class ImmutableVector4f constructor(
    @JvmField inline val v0: Float = 0.0f,
    @JvmField inline val v1: Float = 0.0f,
    @JvmField inline val v2: Float = 0.0f,
    @JvmField inline val v3: Float = 0.0f
) {
    inline val x: Float get() = v0
    inline val y: Float get() = v1
    inline val r: Float get() = v0
    inline val g: Float get() = v1
    inline val z: Float get() = v2
    inline val b: Float get() = v2
    inline val a: Float get() = v3
    fun instance(): Vector4f = Vector4f(v0, v1, v2, v3)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImmutableVector4f
        if (v0 != other.v0) return false
        if (v1 != other.v1) return false
        if (v2 != other.v2) return false
        if (v3 != other.v3) return false
        return true
    }
    override fun hashCode(): Int {
        var result = v0.hashCode()
        result = 31 * result + v1.hashCode()
        result = 31 * result + v2.hashCode()
        result = 31 * result + v3.hashCode()
        return result
    }
}
class Vector4f constructor(
    v0: Float = 0.0f,
    v1: Float = 0.0f,
    v2: Float = 0.0f,
    @JvmField inline var v3: Float = 0.0f
) : Vector3f(v0, v1, v2) {

    constructor(v0: Int = 1, v1: Int = 1, v2: Int = 1, v3: Int) : this(v0.toFloat(), v1.toFloat(), v2.toFloat(), v3.toFloat())
    constructor(other: Vector4f) : this(other.v0, other.v1, other.v2, other.v3)

    inline var a: Float
        get() = v3
        set(value) { v3 = value }

    operator fun invoke(v0: Float, v1: Float, v2: Float, v3: Float) {
        super.invoke(v0, v1, v2)
        this.v3 = v3
    }

    operator fun invoke(v: Vector4f) {
        super.invoke(v)
        this.v3 = v.v3
    }

    override operator fun invoke(jsonString: String) {
        val split = jsonString.split(StringUtils.VALUE_SEPARATOR)
        v0 = try { split[0].toFloat() } catch (e: Exception) { v0 }
        v1 = try { split[1].toFloat() } catch (e: Exception) { v1 }
        v2 = try { split[2].toFloat() } catch (e: Exception) { v2 }
        v3 = try { split[3].toFloat() } catch (e: Exception) { v3 }
    }

    operator fun plus(v: Vector4f) {
        super.invoke(v)
        this.v3 += v.v3
    }

    operator fun minus(v: Vector4f) {
        super.invoke(v)
        this.v3 -= v.v3
    }

    operator fun times(v: Vector4f) {
        super.invoke(v)
        this.v3 *= v.v3
    }

    override operator fun times(dd: Float) {
        super.times(dd)
        this.v3 *= dd
    }

    operator fun div(v: Vector4f) {
        super.div(v)
        this.v3 /= v.v3
    }

    override operator fun div(dd: Float) {
        super.div(dd)
        this.v3 /= dd
    }

    override fun toJsonString(): String = "$v0${StringUtils.VALUE_SEPARATOR}$v1${StringUtils.VALUE_SEPARATOR}$v2${StringUtils.VALUE_SEPARATOR}$v3"
    override fun toString(): String = "[r=$v0${StringUtils.VALUE_SEPARATOR}g=$v1${StringUtils.VALUE_SEPARATOR}b=$v2${StringUtils.VALUE_SEPARATOR}a=$v3]"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as Vector4f

        if (v3 != other.v3) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + v3.hashCode()
        return result
    }
}
