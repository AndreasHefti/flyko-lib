package com.inari.util

import com.inari.firefly.core.component.CompId
import kotlin.jvm.JvmField

interface Named {
    val name: String
}

typealias Call = () -> Unit
typealias Consumer<C> = (C) -> Unit
typealias Supplier<C> = () -> C
typealias Receiver<C> = (C) -> C
typealias Predicate<C> = (C) -> Boolean
typealias IntFunction = (Int) -> Int
typealias IntSupplier = () -> Int
typealias IntConsumer = (Int) -> Unit
typealias IntPredicate = (Int) -> Boolean
typealias ComponentIdFunction = (CompId) -> CompId
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