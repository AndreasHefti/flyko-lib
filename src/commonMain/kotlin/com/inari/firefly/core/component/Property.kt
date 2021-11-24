package com.inari.firefly.core.component

import kotlin.reflect.KMutableProperty0

typealias PropertyAccessor<T> = KMutableProperty0<T>
typealias PropertyRefResolver<T> = (CompId) -> PropertyAccessor<T>
fun <T> NULL_PROPERTY_REF_RESOLVER(): PropertyRefResolver<T> = { _ -> throw IllegalStateException("NULL_FUNCTION") }
