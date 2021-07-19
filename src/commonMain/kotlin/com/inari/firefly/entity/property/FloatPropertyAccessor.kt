package com.inari.firefly.entity.property

interface FloatPropertyAccessor : VirtualPropertyRef.PropertyAccessor {
    fun get(): Float
    fun set(value: Float)
}