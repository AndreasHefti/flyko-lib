package com.inari.util

import com.inari.firefly.core.component.CompId

interface Named {
    val name: String
}

enum class OpResult {
    SUCCESS,
    RUNNING,
    FAILED
}



typealias Consumer<C> = (C) -> Unit
typealias Operation<C> = (C) -> OpResult
typealias UpdateOperation = () -> OpResult
typealias TaskOperation = () -> OpResult
typealias TaskCallback = (OpResult) -> Any
typealias ComponentTaskOperation = (CompId, CompId, CompId) -> OpResult
typealias Call = () -> Unit
typealias Supplier<C> = () -> C
typealias Receiver<C> = (C) -> C
typealias Predicate<C> = (C) -> Boolean

typealias IntFunction = (Int) -> Int
typealias IntOperation = (Int) -> OpResult
typealias IntSupplier = () -> Int
typealias IntConsumer = (Int) -> Unit
typealias IntPredicate = (Int) -> Boolean

typealias FloatFunction = (Float) -> Int
typealias FloatOperation = (Float) -> OpResult
typealias FloatSupplier = () -> Float
typealias FloatConsumer = (Float) -> Unit
typealias FloatPredicate = (Float) -> Boolean

typealias BooleanFunction = (Boolean) -> Boolean
typealias BooleanOperation = (Boolean) -> OpResult
typealias BooleanSupplier = () -> Boolean
typealias BooleanConsumer = (Boolean) -> Unit

typealias ComponentIdFunction = (CompId) -> CompId
typealias ComponentIdOperation = (CompId) -> OpResult
typealias ComponentIdSupplier = () -> CompId
typealias ComponentIdConsumer = (CompId) -> Unit

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