package com.inari.firefly.control.behavior

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.entity.Entity
import com.inari.util.OpResult

abstract class BxNode protected constructor() : SystemComponent(BxNode::class.simpleName!!) {

    abstract fun tick(entity: Entity, behavior: EBehavior): OpResult

    override fun componentType() = Companion
    companion object : SystemComponentType<BxNode>(BxNode::class)
}