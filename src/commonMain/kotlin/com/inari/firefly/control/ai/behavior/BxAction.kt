package com.inari.firefly.control.ai.behavior

import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class BxAction private constructor() : BxNode() {

    @JvmField var tickOp: BxOp = SUCCESS_BX_OPERATION
    var state: Aspect = UNDEFINED_BEHAVIOR_STATE
        set(value) =
            if (BEHAVIOR_STATE_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    override fun tick(entity: Entity, behavior: EBehavior): OpResult {
        val result = tickOp(entity, behavior)
        if (result === OpResult.SUCCESS)
            behavior.actionsDone + state
        return result
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxAction>(BxNode, BxAction::class) {
        override fun createEmpty() = BxAction()
    }

}