package com.inari.util

import com.inari.util.geom.ImmutableVector2f
import com.inari.util.geom.ImmutableVector2i
import com.inari.util.geom.ImmutableVector4f
import kotlin.jvm.JvmField

const val ZERO_INT = 0
const val ZERO_FLOAT = 0.0f
const val UNDERLINE = "_"
const val JSON_NO_VALUE = "-"
const val COLON = ":"
const val EMPTY_STRING = ""
const val COMMA = ","
const val VALUE_SEPARATOR = ','
const val VALUE_SEPARATOR_STRING = ","
const val LINE_BRAKE = '\n'

const val KEY_VALUE_SEPARATOR = '='
const val KEY_VALUE_SEPARATOR_STRING = "="

const val LIST_VALUE_SEPARATOR = '|'
const val LIST_VALUE_SEPARATOR_STRING = "|"

const val NO_NAME = "[[NO_NAME]]"
const val NO_STATE: String = "[[NO_STATE]]"
const val NO_PROGRAM: String = "[[NO_PROGRAM]]"

const val LONG_MASK = 0x3f

@JvmField val VEC_NF_NORTH = ImmutableVector2f(0f, 1f)
@JvmField val VEC_NF_SOUTH = ImmutableVector2f(0f, -1f)
@JvmField val VEC_NF_WEST = ImmutableVector2f(-1f, 0f)
@JvmField val VEC_NF_EAST = ImmutableVector2f(1f, 0f)

@JvmField val VEC_NI_NORTH = ImmutableVector2i(0, 1)
@JvmField val VEC_NI_SOUTH = ImmutableVector2i(0, -1)
@JvmField val VEC_NI_WEST = ImmutableVector2i(-1, 0)
@JvmField val VEC_NI_EAST = ImmutableVector2i(1, 0)

@JvmField val INT_FUNCTION_IDENTITY: (Int) -> Int = { i -> i }
@JvmField val INT_FUNCTION_ZERO: (Int) -> Int = { _ -> 0 }
@JvmField val DO_NOTHING = {}
@JvmField val FALSE_SUPPLIER = { false }
@JvmField val TRUE_SUPPLIER = { true }
@JvmField val FALSE_PREDICATE: (Any) -> Boolean = { false }
@JvmField val TRUE_PREDICATE: (Any) -> Boolean = { true }
@JvmField val VOID_CALL: () -> Unit = {}

@JvmField val BLACK = ImmutableVector4f(0f, 0f, 0f, 1f)
@JvmField val WHITE = ImmutableVector4f(1f, 1f, 1f, 1f)

interface Named {
    val name: String
}

interface FloatPropertyAccessor {
    operator fun invoke(value: Float)
}

interface IntPropertyAccessor {
    operator fun invoke(value: Int)
}

val VOID_FLOAT_PROPERTY_ACCESSOR = object : FloatPropertyAccessor {
    override fun invoke(value: Float) {}
}
val VOID_INT_PROPERTY_ACCESSOR = object : IntPropertyAccessor {
    override inline fun invoke(value: Int) {}
}

val VOID_FLOAT_PROPERTY_ACCESSOR_PROVIDER: (Int) -> FloatPropertyAccessor = { _ -> VOID_FLOAT_PROPERTY_ACCESSOR }
val VOID_INT_PROPERTY_ACCESSOR_PROVIDER: (Int) -> IntPropertyAccessor = { _ -> VOID_INT_PROPERTY_ACCESSOR }

@JvmField val VOID_CONSUMER_0 = {}
@JvmField val VOID_CONSUMER_1: (Any) -> Unit = { _ -> }
@JvmField val VOID_CONSUMER_2: (Any, Any) -> Unit = { _, _ -> }
@JvmField val VOID_CONSUMER_3: (Any, Any, Any) -> Unit = { _, _, _ -> }
@JvmField val VOID_INT_CONSUMER: (Int) -> Unit = VOID_CONSUMER_1
