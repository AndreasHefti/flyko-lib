package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_COMPONENT_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.Call
import com.inari.util.ComponentTaskOperation
import com.inari.util.OpResult
import com.inari.util.TaskOperation

class GenericTask private constructor() : Task() {

    private var operation: ComponentTaskOperation = EMPTY_COMPONENT_TASK_OPERATION
    fun withOperation(op: ComponentTaskOperation) {
        operation = op
    }
    fun withSimpleOperation(op: Call) {
        operation = { _, _, _ ->
            op()
            OpResult.SUCCESS
        }
    }

    override fun invoke(compId1: CompId, compId2: CompId, compId3: CompId): OpResult {
        val result = operation(compId1, compId2, compId3)
        if (removeAfterRun)
            FFContext.delete(this)
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Task, GenericTask>(Task, GenericTask::class) {
        override fun createEmpty() = GenericTask()
    }

}