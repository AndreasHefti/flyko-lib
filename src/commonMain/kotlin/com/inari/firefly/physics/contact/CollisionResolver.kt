package com.inari.firefly.physics.contact

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.entity.Entity

abstract class CollisionResolver protected constructor() : SystemComponent(CollisionResolver::class.simpleName!!) {

    abstract fun resolve(entity: Entity, contact: EContact, contactScan: ContactScans)

    companion object : SystemComponentType<CollisionResolver>(CollisionResolver::class)
}