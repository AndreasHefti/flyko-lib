package com.inari.firefly.control.task

import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class ParallelTask private constructor() : Task() {

    private var operation: ActionOperation = EMPTY_OP
    private val callback: TaskCallback = EMPTY_TASK_CALLBACK

    fun withOperation(op: ActionOperation) {
        operation = op
    }
    fun withSimpleOperation(op: Call) {
        operation = { _, _, _ ->
            op()
            OpResult.SUCCESS
        }
    }

    override fun invoke(compId1: Int, compId2: Int, compId3: Int): OpResult {
        startParallelTask(
            name,
            { operation(compId1, compId2, compId3) },
            { _, r -> if (r)
                callback(this.componentId.index, OpResult.SUCCESS)
            else
                callback(this.componentId.index, OpResult.FAILED) }
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