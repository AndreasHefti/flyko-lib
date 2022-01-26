package com.inari.firefly.control.behavior

import com.inari.firefly.control.BxConditionOp
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.TRUE_CONDITION
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import kotlin.jvm.JvmField

class BxCondition private constructor() : BxNode() {

    @JvmField var condition: BxConditionOp = TRUE_CONDITION

    override fun tick(entity: Entity, behavior: EBehavior): OpResult =
            when (condition(entity, behavior) ) {
                true -> OpResult.SUCCESS
                false -> OpResult.FAILED
            }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxCondition>(BxNode, BxCondition::class) {
        override fun createEmpty() = BxCondition()
    }

}