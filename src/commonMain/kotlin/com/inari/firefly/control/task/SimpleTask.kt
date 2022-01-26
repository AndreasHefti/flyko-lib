package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.control.ComponentTaskOperation
import com.inari.firefly.control.EMPTY_COMPONENT_TASK_OPERATION
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class SimpleTask private constructor() : Task() {

    private var operation: ComponentTaskOperation = EMPTY_COMPONENT_TASK_OPERATION
    fun withOperation(op: ComponentTaskOperation) {
        operation = op
    }
    fun withOperation(op: ComponentIdConsumer) {
        operation = { id, _, _ ->
            op(id)
            OpResult.SUCCESS
        }
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
    companion object : SystemComponentSubType<Task, SimpleTask>(Task, SimpleTask::class) {
        override fun createEmpty() = SimpleTask()
    }

}