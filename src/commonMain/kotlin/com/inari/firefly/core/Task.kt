package com.inari.firefly.core

import com.inari.util.*
import kotlin.jvm.JvmField

class Task private constructor(): Component(Task) {

    @JvmField var deleteAfterRun: Boolean = false
    @JvmField var noneBlocking: Boolean = false
    @JvmField var operation: TaskOperation = SUCCESS_TASK_OPERATION
    @JvmField var callback: TaskCallback = VOID_TASK_CALLBACK

    operator fun invoke(compId1: Int = -1, compId2: Int = -1, compId3: Int = -1): OperationResult {
        return if (noneBlocking) {
            startParallelTask(
                name,
                { operation(compId1, compId2, compId3) },
                { _, r ->
                    if (r)
                        callback(this.index, OperationResult.SUCCESS)
                    else
                        callback(this.index, OperationResult.FAILED)
                }
            )
            if (deleteAfterRun) Task.delete(this)
            OperationResult.RUNNING
        } else {
            val result = operation(compId1, compId2, compId3)
            if (deleteAfterRun) Task.delete(this)
            result
        }
    }

    companion object : ComponentSystem<Task>("Task") {
        override fun allocateArray(size: Int): Array<Task?> = arrayOfNulls(size)
        override fun create() = Task()
    }
}