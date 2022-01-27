package com.inari.firefly.control.ai.behavior

import com.inari.firefly.control.BxConditionOp
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.TRUE_BX_CONDITION
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class BxCondition private constructor() : BxNode() {

    @JvmField var condition: BxConditionOp = TRUE_BX_CONDITION

    override fun tick(entity: Entity, behavior: EBehavior): OpResult =
            when (condition(entity, behavior) ) {
                true -> OpResult.SUCCESS
                false -> OpResult.FAILED
            }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxCondition>(BxNode, BxCondition::class) {
        override fun createEmpty() = BxCondition()

        fun actionDoneCondition(aspect: Aspect): BxConditionOp = {
                _, bx -> aspect in bx.actionsDone
        }
    }

}