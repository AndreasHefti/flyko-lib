package com.inari.firefly.control.behavior

import com.inari.firefly.control.behavior.BehaviorSystem.SUCCESS_ACTION
import com.inari.firefly.control.behavior.BehaviorSystem.UNDEFINED_BEHAVIOR_STATE
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.OpResult
import com.inari.util.aspect.Aspect

class BxAction private constructor() : BxNode() {

    var tickOp: BxOp = SUCCESS_ACTION
    var state: Aspect = UNDEFINED_BEHAVIOR_STATE
        set(value) =
            if (BehaviorSystem.BEHAVIOR_STATE_ASPECT_GROUP.typeCheck(value)) field = value
            else throw IllegalArgumentException()

    override fun tick(entity: Entity, behavior: EBehavior): OpResult {
        val result = tickOp(entity, behavior)
        if (result === OpResult.SUCCESS)
            behavior.actionsDone + state
        return result
    }

    companion object : SystemComponentSubType<BxNode, BxAction>(BxNode, BxAction::class) {
        override fun createEmpty() = BxAction()
    }

}