package com.inari.firefly.control.ai.behavior

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity

class BxSelection private constructor() : BxBranch() {

    override fun tick(entityId: Int): OpResult {
        var i = 0
        loop@ while (i < children.capacity) {
            when(children[i++]?.tick(entityId) ?: continue@loop) {
                OpResult.RUNNING -> return OpResult.RUNNING
                OpResult.SUCCESS -> return OpResult.SUCCESS
                OpResult.FAILED -> DO_NOTHING
            }
        }
        return OpResult.FAILED
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxSelection>(BxNode, BxSelection::class) {
        override fun createEmpty() = BxSelection()
    }

}