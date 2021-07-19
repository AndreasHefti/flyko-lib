package com.inari.firefly.entity.property

interface IntPropertyAccessor : VirtualPropertyRef.PropertyAccessor {
    fun get(): Int
    fun set(value: Int)
}