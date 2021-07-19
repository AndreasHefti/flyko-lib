package com.inari.firefly.core.component

import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO


class ArrayAccessor<T>(
    private val array: DynArray<T>,
    private val trim: Boolean = false
) {
    val dynArray: DynArrayRO<T> = array
    fun add(t: T) {
        array.add(t)
        if (trim) array.trim()
    }
    fun addAll(a: Collection<T>) {
        for (value in a)
            array.add(value)
        if (trim) array.trim()
    }
    fun addAll(a: DynArray<T>) {
        array.addAll(a)
        if (trim) array.trim()
    }
    fun addAll(vararg a: T) {
        a.forEach { t -> array.add(t) }
        if (trim) array.trim()
    }
    fun set(t: T, index: Int) {
        array[index] = t
        if (trim) array.trim()
    }
    fun delete(t: T) {
        array.remove(t)
        if (trim) array.trim()
    }
    fun delete(index: Int) {
        array.remove(index)
        if (trim) array.trim()
    }
}