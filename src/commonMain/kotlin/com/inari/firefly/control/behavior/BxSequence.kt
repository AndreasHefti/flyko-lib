package com.inari.firefly.control.behavior

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import com.inari.util.OpResult

class BxSequence private constructor() : BxBranch() {

    override fun tick(entity: Entity, behavior: EBehavior): OpResult {
        var i = 0
        loop@ while (i < children.capacity) {
            when(children[i++]?.tick(entity, behavior) ?: continue@loop) {
                OpResult.RUNNING -> return OpResult.RUNNING
                OpResult.FAILED -> return OpResult.FAILED
                OpResult.SUCCESS -> DO_NOTHING
            }
        }
        return OpResult.SUCCESS
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxSequence>(BxNode, BxSequence::class) {
        override fun createEmpty() = BxSequence()
    }
}