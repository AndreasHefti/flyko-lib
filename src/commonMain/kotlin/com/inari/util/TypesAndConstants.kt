package com.inari.util

import com.inari.firefly.core.Component
import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.api.ButtonType
import com.inari.util.geom.ImmutableVector2f
import com.inari.util.geom.ImmutableVector2i
import com.inari.util.geom.ImmutableVector4f
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

const val ZERO_INT = 0
const val ZERO_FLOAT = 0.0f
const val UNDERLINE = "_"
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

@JvmField val BLACK = ImmutableVector4f(0f, 0f, 0f, 1f)
@JvmField val WHITE = ImmutableVector4f(1f, 1f, 1f, 1f)

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

interface Dictionary : Iterable<String> {
    operator fun contains(name: String): Boolean
    operator fun invoke(name: String): String?
    fun names(): Iterator<String>
    override fun iterator(): Iterator<String> = names()
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

enum class OperationResult {
    SUCCESS,
    RUNNING,
    FAILED
}

//typealias AttributeMap = (String) -> String?
typealias Operation = () -> OperationResult
typealias OperationCallback = (OperationResult) -> Unit
typealias TaskOperation = (ComponentKey, Dictionary) -> OperationResult
typealias TaskCallback = (ComponentKey, Dictionary, OperationResult) -> Unit
typealias ConditionalOperation = (Int, Int, Int) -> Boolean
typealias NormalOperation = (Int, Int, Int) -> Float
/** { entityId, velocity, button -> } */
typealias MoveCallback = (Int, Float, ButtonType) -> Unit
val EMPTY_DICTIONARY: Dictionary = object : Dictionary {
    override fun contains(name: String): Boolean = false
    override fun invoke(name: String): String? = null
    override fun names(): Iterator<String> = emptyList<String>().iterator()
}
val VOID_MOVE_CALLBACK: MoveCallback = { _, _, _ -> }
val VOID_TASK_CALLBACK: TaskCallback = { _, _, _ -> }
val RUNNING_OPERATION: Operation = { OperationResult.RUNNING }
val SUCCESS_OPERATION: Operation = { OperationResult.SUCCESS }
val FAILED_OPERATION: Operation = { OperationResult.FAILED }
val RUNNING_TASK_OPERATION: TaskOperation = {  _, _ -> OperationResult.RUNNING }
val SUCCESS_TASK_OPERATION: TaskOperation = {  _, _ -> OperationResult.SUCCESS }
val FAILED_TASK_OPERATION: TaskOperation = {  _, _ -> OperationResult.FAILED }
val TRUE_OPERATION: ConditionalOperation = { _, _, _ -> true }
val FALSE_OPERATION: ConditionalOperation = { _, _, _ -> false }
@JvmField val ZERO_OP: NormalOperation = { _, _, _ -> 0f }
@JvmField val ONE_OP : NormalOperation = { _, _, _ -> 1f }

operator fun ConditionalOperation.invoke() = this(-1, -1, -1)
operator fun ConditionalOperation.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun ConditionalOperation.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)
operator fun TaskOperation.invoke() = this(NO_COMPONENT_KEY, EMPTY_DICTIONARY)
operator fun TaskOperation.invoke(key: ComponentKey) = this(key, EMPTY_DICTIONARY)
operator fun NormalOperation.invoke() = this(-1, -1, -1)
operator fun NormalOperation.invoke(entityId: Int) = this(entityId, -1, -1)
operator fun NormalOperation.invoke(entityId1: Int, entityId2: Int) = this(entityId1, entityId2, -1)

class Attributes(val map: Map<String, String>) : Dictionary {
    override fun contains(name: String): Boolean = map.containsKey(name)
    override fun invoke(name: String): String? = map[name]
    override fun names(): Iterator<String> = map.keys.iterator()
}