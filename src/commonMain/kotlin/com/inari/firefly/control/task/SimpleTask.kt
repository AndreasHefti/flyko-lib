package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.control.*
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class SimpleTask private constructor() : Task() {

    private var operation: ActionOperation = EMPTY_OP
    fun withOperation(op: ActionOperation) {
        operation = op
    }
    fun withOperation(op: IntConsumer) {
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

    override fun invoke(compId1: Int, compId2: Int, compId3: Int): OpResult {
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