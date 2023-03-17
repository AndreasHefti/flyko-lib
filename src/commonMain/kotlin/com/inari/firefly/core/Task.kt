package com.inari.firefly.core

import com.inari.firefly.core.api.*
import com.inari.util.VOID_CALL
import com.inari.util.collection.Dictionary
import com.inari.util.collection.EMPTY_DICTIONARY
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField

open class Task protected constructor(): Component(Task) {

    @JvmField var deleteAfterRun: Boolean = false
    @JvmField var noneBlocking: Boolean = false
    @JvmField var simpleTask: SimpleTask = VOID_CALL
    @JvmField var operation: TaskOperation = VOID_TASK_OPERATION
    @JvmField var callback: TaskCallback = VOID_TASK_CALLBACK
    @JvmField var attributes: Dictionary = EMPTY_DICTIONARY

    operator fun invoke(
        componentKey: ComponentKey = NO_COMPONENT_KEY,
        attributes: Dictionary = EMPTY_DICTIONARY) {

        if (simpleTask != VOID_CALL)
            simpleTask()

        if (operation == VOID_TASK_OPERATION)
            return

        val dict = if (this.attributes != EMPTY_DICTIONARY)
            this.attributes + attributes
            else attributes


        if (noneBlocking)
            startParallelTask(name, { operation(componentKey, dict, callback) }) {
                if (it != null)
                    callback(componentKey, dict, OperationResult.FAILED)
            }
        else
            operation(componentKey, dict, callback)

        if (deleteAfterRun) Task.delete(this)
    }

    companion object : ComponentSystem<Task>("Task") {
        override fun allocateArray(size: Int): Array<Task?> = arrayOfNulls(size)
        override fun create() = Task()
    }
}

abstract class StaticTask protected constructor() : Task() {

    init {
        super.operation = { key, attributes, callback ->  this.apply(key, attributes, callback) }
        Task.registerAsSingleton(this, true)
        Task.activate(this.index)
    }

    protected abstract fun apply(
        key : ComponentKey,
        attributes: Dictionary = EMPTY_DICTIONARY,
        callback: TaskCallback)
}