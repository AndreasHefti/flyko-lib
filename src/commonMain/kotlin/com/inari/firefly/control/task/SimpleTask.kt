package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.OpResult
import com.inari.util.TaskOperation
import kotlin.jvm.JvmField

class SimpleTask private constructor() : SystemComponent(SimpleTask::class.simpleName!!), TriggeredSystemComponent {

    @JvmField var removeAfterRun: Boolean = false
    private var operation: TaskOperation = EMPTY_TASK_OPERATION
    fun withTaskOperation(op: TaskOperation) {
        operation = op
    }

    fun invoke(): OpResult {
        val result = operation()
        if (removeAfterRun)
            FFContext.delete(this)
        return result
    }

    override fun <A : Trigger> withTrigger(
        cBuilder: SystemComponentBuilder<A>,
        configure: (A.() -> Unit)): A  {

        val result = super.withTrigger(cBuilder, configure)
        result.call = { invoke() }
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<SimpleTask>(SimpleTask::class) {
        override fun createEmpty() = SimpleTask()
    }
}