package com.inari.firefly.physics.contact

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.entity.Entity
import kotlin.jvm.JvmField

abstract class CollisionResolver protected constructor() : SystemComponent(CollisionResolver::class.simpleName!!) {

    @JvmField internal var separateDirections = true
    @JvmField internal var yDirectionFirst = true

    abstract fun resolve(entity: Entity)

    override fun componentType() = Companion
    companion object : SystemComponentType<CollisionResolver>(CollisionResolver::class)
}