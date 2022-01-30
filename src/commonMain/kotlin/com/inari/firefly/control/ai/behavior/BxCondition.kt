package com.inari.firefly.control.ai.behavior

import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class BxCondition private constructor() : BxNode() {

    @JvmField var condition: ConditionOperation = TRUE_OP

    override fun tick(entityId: Int): OpResult =
            when (condition(entityId) ) {
                true -> OpResult.SUCCESS
                false -> OpResult.FAILED
            }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxCondition>(BxNode, BxCondition::class) {
        override fun createEmpty() = BxCondition()
    }

}