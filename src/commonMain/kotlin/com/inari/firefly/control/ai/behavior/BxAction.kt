package com.inari.firefly.control.ai.behavior

import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class BxAction private constructor() : BxNode() {

    @JvmField var action: EntityAction = EMPTY_ENTITY_ACTION

    override fun tick(entityId: Int): OpResult = action(entityId)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxAction>(BxNode, BxAction::class) {
        override fun createEmpty() = BxAction()
    }

}