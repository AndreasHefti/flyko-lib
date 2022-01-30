package com.inari.firefly.control.ai.behavior

import com.inari.firefly.control.OpResult
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType

abstract class BxNode protected constructor() : SystemComponent(BxNode::class.simpleName!!) {

    abstract fun tick(entityId: Int): OpResult

    companion object : SystemComponentType<BxNode>(BxNode::class)
}