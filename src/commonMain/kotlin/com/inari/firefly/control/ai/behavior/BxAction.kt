package com.inari.firefly.control.ai.behavior


import com.inari.firefly.control.ActionOperation
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.EMPTY_OP
import com.inari.firefly.control.invoke
import com.inari.firefly.core.system.SystemComponentSubType
import kotlin.jvm.JvmField

class BxAction private constructor() : BxNode() {

    @JvmField var actionOperation: ActionOperation = EMPTY_OP

    override fun tick(entityId: Int): OpResult = actionOperation(entityId)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<BxNode, BxAction>(BxNode, BxAction::class) {
        override fun createEmpty() = BxAction()
    }

}