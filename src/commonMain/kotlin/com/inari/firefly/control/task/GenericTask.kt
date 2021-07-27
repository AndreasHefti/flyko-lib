package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.Call
import com.inari.util.OpResult
import com.inari.util.TaskOperation

class GenericTask private constructor() : SystemComponent(GenericTask::class.simpleName!!), TriggeredSystemComponent {

    private val triggerCall: Call = { invoke() }

    var removeAfterRun: Boolean = false
    var operation: TaskOperation = EMPTY_TASK_OPERATION

    fun invoke(): OpResult {
        val result = operation()
        if (removeAfterRun)
            FFContext.delete(this)
        return result
    }

    override fun <A : Trigger> withTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A  {
        val result = super.withTrigger(cBuilder, configure)
        result.call = triggerCall
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<GenericTask>(GenericTask::class) {
        override fun createEmpty() = GenericTask()
    }

}