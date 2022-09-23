package com.inari.util

import com.inari.firefly.core.api.FFTimer
import com.inari.util.geom.ImmutableVector2f
import com.inari.util.geom.ImmutableVector2i
import kotlin.jvm.JvmField

const val ZERO_INT = 0
const val ZERO_FLOAT = 0.0f

const val NO_NAME = "[[NO_NAME]]"
const val NO_STATE: String = "[[NO_STATE]]"
const val NO_PROGRAM: String = "[[NO_PROGRAM]]"

// normalized vecrtors

@JvmField val VEC_NF_NORTH = ImmutableVector2f(0f, 1f)
@JvmField val VEC_NF_SOUTH = ImmutableVector2f(0f, -1f)
@JvmField val VEC_NF_WEST = ImmutableVector2f(-1f, 0f)
@JvmField val VEC_NF_EAST = ImmutableVector2f(1f, 0f)

@JvmField val VEC_NI_NORTH = ImmutableVector2i(0, 1)
@JvmField val VEC_NI_SOUTH = ImmutableVector2i(0, -1)
@JvmField val VEC_NI_WEST = ImmutableVector2i(-1, 0)
@JvmField val VEC_NI_EAST = ImmutableVector2i(1, 0)

@JvmField val VOID_INT_CONSUMER: (Int) -> Unit = { _ -> }
@JvmField val INT_FUNCTION_IDENTITY: (Int) -> Int = { i -> i }
@JvmField val INT_FUNCTION_NULL: (Int) -> Int = { _ -> 0 }
@JvmField val NULL_INT_FUNCTION: (Int) -> Int = { _ -> throw IllegalStateException("NULL_INT_FUNCTION") }
@JvmField val NULL_INT_CONSUMER: (Int) -> Unit = { _ -> throw IllegalStateException("NULL_INT_CONSUMER") }
@JvmField val NULL_CONSUMER: (Any) -> Unit = { _ -> throw IllegalStateException("NULL_CONSUMER") }
@JvmField val NULL_CALL: () -> Unit = { throw IllegalStateException("NULL_CALL called") }
@JvmField val DO_NOTHING = {}
@JvmField val FALSE_SUPPLIER = { false }
@JvmField val TRUE_SUPPLIER = { true }
@JvmField val FALSE_PREDICATE: (Any) -> Boolean = { false }
@JvmField val TRUE_PREDICATE: (Any) -> Boolean = { true }
@JvmField val VOID_CONSUMER: (Any) -> Unit = { _ -> }
@JvmField val VOID_CALL: () -> Unit = {}
@JvmField val INFINITE_SCHEDULER: FFTimer.Scheduler = object : FFTimer.Scheduler {
    override val resolution: Float = 60f
    override fun needsUpdate(): Boolean = true
}

interface Named {
    val name: String
}

/** Use this on types that can be disposed  */
interface Disposable {
    /** Dispose the instance  */
    fun dispose()
}

interface Loadable {
    fun load(): Disposable
}

/** Use this for types that can be cleared or are used in an abstract service that used to
 * clear object(s) but do not have to know the exact subType of the object(s)
 */
interface Clearable {
    /** Clears the instance  */
    fun clear()
}

interface IntIterator {
    operator fun hasNext(): Boolean
    operator fun next(): Int
}

interface FloatPropertyAccessor {
    operator fun invoke(value: Float)
    operator fun invoke(): Float
}

interface IntPropertyAccessor {
    operator fun invoke(value: Int)
    operator fun invoke(): Int
}

val VOID_FLOAT_PROPERTY_ACCESSOR = object : FloatPropertyAccessor {
    override fun invoke(value: Float) {}
    override fun invoke(): Float = ZERO_FLOAT
}
val VOID_INT_PROPERTY_ACCESSOR = object : IntPropertyAccessor {
    override fun invoke(value: Int) {}
    override fun invoke(): Int = ZERO_INT
}

val VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER: (Int) -> FloatPropertyAccessor = { _ -> VOID_FLOAT_PROPERTY_ACCESSOR }
val VOID_INT_PROPERTY_ACCESSOR_PROVIDER: (Int) -> IntPropertyAccessor = { _ -> VOID_INT_PROPERTY_ACCESSOR }