package com.inari.firefly.entity.property

interface ValuePropertyAccessor<T> : VirtualPropertyRef.PropertyAccessor {
    fun get(): T
    fun set(value: T)
}