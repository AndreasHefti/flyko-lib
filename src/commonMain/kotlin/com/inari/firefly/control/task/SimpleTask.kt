package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.control.EMPTY_ENTITY_ACTION_CALL
import com.inari.firefly.control.EntityActionCall
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.*

class SimpleTask private constructor() : Task() {

    private var action: EntityActionCall = EMPTY_ENTITY_ACTION_CALL
    fun withOperation(op: EntityActionCall) {
        action = op
    }
    fun withOperation(op: IntConsumer) {
        action = { id, _, _ ->
            op(id)
            OpResult.SUCCESS
        }
    }
    fun withSimpleOperation(op: Call) {
        action = { _, _, _ ->
            op()
            OpResult.SUCCESS
        }
    }

    override fun invoke(compId1: Int, compId2: Int, compId3: Int): OpResult {
        val result = action(compId1, compId2, compId3)
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