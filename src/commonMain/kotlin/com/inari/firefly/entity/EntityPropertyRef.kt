package com.inari.firefly.entity

import com.inari.util.PropertyAccessor
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.util.Named

class EntityPropertyRef<T>(
    val accessor: (Entity) -> PropertyAccessor<T>
) {
    operator fun invoke(id : Int) : PropertyAccessor<T> = this(FFContext[Entity, id])
    operator fun invoke(name : String) : PropertyAccessor<T> = this(FFContext[Entity, name])
    operator fun invoke(compId: CompId) : PropertyAccessor<T> = this(FFContext[compId])
    operator fun invoke(named : Named) : PropertyAccessor<T> = this(FFContext[Entity, named])
    operator fun invoke(entity: Entity): PropertyAccessor<T> = accessor(entity)
}