package com.inari.firefly.entity.property

import com.inari.firefly.entity.Entity
import kotlin.reflect.KClass

interface VirtualPropertyRef {

    val propertyName: String
    val type: KClass<*>
    fun accessor(entity: Entity): PropertyAccessor

    interface PropertyAccessor

}