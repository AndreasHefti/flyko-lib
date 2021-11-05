package com.inari.firefly.control.task

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.EMPTY_COMPONENT_TASK_OPERATION
import com.inari.firefly.VOID_CONSUMER
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class ParallelTask private constructor() : Task() {

    private var operation: ComponentTaskOperation = EMPTY_COMPONENT_TASK_OPERATION
    private val callback: TaskCallback = VOID_CONSUMER

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
        startParallelTask(
            name,
            { operation(compId1, compId2, compId3) },
            { _, r -> if (r) callback(OpResult.SUCCESS) else callback(OpResult.FAILED) }
        )
        return OpResult.RUNNING
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Task, ParallelTask>(Task, ParallelTask::class) {
        override fun createEmpty() = ParallelTask()
    }
}