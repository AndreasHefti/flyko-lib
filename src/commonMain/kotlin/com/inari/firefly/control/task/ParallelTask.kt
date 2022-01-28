package com.inari.firefly.control.task

import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class ParallelTask private constructor() : Task() {

    private var action: EntityActionCall = EMPTY_ENTITY_ACTION_CALL
    private val callback: TaskCallback = EMPTY_TASK_CALLBACK

    fun withOperation(op: EntityActionCall) {
        action = op
    }
    fun withSimpleOperation(op: Call) {
        action = { _, _, _ ->
            op()
            OpResult.SUCCESS
        }
    }

    override fun invoke(compId1: Int, compId2: Int, compId3: Int): OpResult {
        startParallelTask(
            name,
            { action(compId1, compId2, compId3) },
            { _, r -> if (r)
                callback(this.componentId, OpResult.SUCCESS)
            else
                callback(this.componentId, OpResult.FAILED) }
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