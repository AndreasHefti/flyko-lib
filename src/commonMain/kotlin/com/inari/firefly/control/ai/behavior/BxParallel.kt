package com.inari.firefly.control.ai.behavior

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.Entity
import kotlin.jvm.JvmField

class BxParallel private constructor() : BxBranch() {

    @JvmField var successThreshold: Int = 0

    override fun tick(entityId: Int): OpResult {
        val threshold = if (successThreshold > children.size)
                children.size
            else
                successThreshold

        var successCount = 0
        var failuresCount = 0
        var i = 0
        loop@ while (i < children.capacity) {
            when(children[i++]?.tick(entityId) ?: continue@loop) {
                OpResult.RUNNING -> DO_NOTHING
                OpResult.SUCCESS -> successCount++
                OpResult.FAILED -> failuresCount++
            }
        }

        return when {
            successCount >= threshold -> OpResult.SUCCESS
            failuresCount > 0 -> OpResult.FAILED
            else -> OpResult.RUNNING
        }
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxParallel>(BxNode, BxParallel::class) {
        override fun createEmpty() = BxParallel()
    }

}