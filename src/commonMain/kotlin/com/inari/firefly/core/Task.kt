package com.inari.firefly.core

import com.inari.util.*
import com.inari.util.collection.Dictionary
import com.inari.util.collection.EMPTY_DICTIONARY
import kotlin.jvm.JvmField

class Task private constructor(): Component(Task) {

    @JvmField var deleteAfterRun: Boolean = false
    @JvmField var noneBlocking: Boolean = false
    @JvmField var operation: TaskOperation = SUCCESS_TASK_OPERATION
    @JvmField var callback: TaskCallback = VOID_TASK_CALLBACK

    fun withSimpleOperation(op: (ComponentKey) -> Unit) {
        operation = { i1, _ ->
            op(i1)
            OperationResult.SUCCESS
        }
    }
    fun withVoidOperation(op: () -> Unit) {
        operation = { _, _ ->
            op()
            OperationResult.SUCCESS
        }
    }

    operator fun invoke(
        compKey: ComponentKey = NO_COMPONENT_KEY,
        attributes: Dictionary = EMPTY_DICTIONARY): OperationResult {

        return if (noneBlocking) {
            startParallelTask(
                name,
                { operation(compKey, attributes) },
                { _, r ->
                    if (r) callback(compKey, attributes, OperationResult.SUCCESS)
                    else callback(compKey, attributes, OperationResult.FAILED)
                }
            )
            if (deleteAfterRun) Task.delete(this)
            OperationResult.RUNNING
        } else {
            val result = operation(compKey, attributes)
            if (deleteAfterRun) Task.delete(this)
            result
        }
    }

    companion object : ComponentSystem<Task>("Task") {
        override fun allocateArray(size: Int): Array<Task?> = arrayOfNulls(size)
        override fun create() = Task()
    }
}