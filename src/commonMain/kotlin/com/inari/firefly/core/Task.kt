package com.inari.firefly.core

import com.inari.firefly.core.api.*
import com.inari.util.VOID_CALL
import com.inari.util.collection.Attributes
import com.inari.util.collection.AttributesRO
import com.inari.util.collection.AttributesRO.Companion.EMPTY_ATTRIBUTES
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField

open class Task protected constructor(): Component(Task) {

    @JvmField var deleteAfterRun: Boolean = false
    @JvmField var noneBlocking: Boolean = false
    @JvmField var simpleTask: SimpleTask = VOID_CALL
    @JvmField var operation: TaskOperation = VOID_TASK_OPERATION
    @JvmField var callback: TaskCallback = VOID_TASK_CALLBACK
    @JvmField var attributes: AttributesRO = EMPTY_ATTRIBUTES

    private val tempAttributes = Attributes()
    operator fun invoke(
        componentKey: ComponentKey = NO_COMPONENT_KEY,
        attributes: AttributesRO = EMPTY_ATTRIBUTES) {

        if (simpleTask != VOID_CALL)
            simpleTask()

        if (operation == VOID_TASK_OPERATION)
            return

        tempAttributes.clear()
        val dict = if (this.attributes != EMPTY_ATTRIBUTES)
            tempAttributes + this.attributes + attributes
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
        @Suppress("LeakingThis") Task.registerStatic(this)
        Task.activate(this.index)
    }

    protected abstract fun apply(
        key : ComponentKey,
        attributes: AttributesRO = EMPTY_ATTRIBUTES,
        callback: TaskCallback)
}