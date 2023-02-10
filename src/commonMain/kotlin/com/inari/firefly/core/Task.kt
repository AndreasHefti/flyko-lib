package com.inari.firefly.core

import com.inari.firefly.core.api.*
import com.inari.util.VOID_CONSUMER_1
import com.inari.util.collection.Dictionary
import com.inari.util.collection.EMPTY_DICTIONARY
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField

open class Task protected constructor(): Component(Task) {

    @JvmField var deleteAfterRun: Boolean = false
    @JvmField var noneBlocking: Boolean = false
    @JvmField var simpleTask: SimpleTask = VOID_CONSUMER_1
    @JvmField var operation: TaskOperation = VOID_TASK_OPERATION
    @JvmField var callback: TaskCallback = VOID_TASK_CALLBACK
    @JvmField var attributes: Dictionary = EMPTY_DICTIONARY

    operator fun invoke(
        compIndex: ComponentIndex = NULL_COMPONENT_INDEX,
        attributes: Dictionary = EMPTY_DICTIONARY) {

        if (simpleTask != VOID_CONSUMER_1) {
            simpleTask(compIndex)
        }

        if (operation == VOID_TASK_OPERATION)
            return

        val dict = if (this.attributes != EMPTY_DICTIONARY)
            this.attributes + attributes
            else attributes


        if (noneBlocking)
            startParallelTask(name) { operation(compIndex, dict, callback) }
        else
            operation(compIndex, dict, callback)

        if (deleteAfterRun) Task.delete(this)
    }

    companion object : ComponentSystem<Task>("Task") {
        override fun allocateArray(size: Int): Array<Task?> = arrayOfNulls(size)
        override fun create() = Task()
    }
}