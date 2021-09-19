package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_COMPONENT_TASK_OPERATION
import com.inari.firefly.EMPTY_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.Call
import com.inari.util.ComponentTaskOperation
import com.inari.util.OpResult
import com.inari.util.TaskOperation
import kotlin.jvm.JvmField

class GenericTask private constructor() : SystemComponent(GenericTask::class.simpleName!!), TriggeredSystemComponent {

    @JvmField var removeAfterRun: Boolean = false
    private var operation: ComponentTaskOperation = EMPTY_COMPONENT_TASK_OPERATION
    fun withTaskOperation(op: ComponentTaskOperation) {
        operation = op
    }

    fun invoke(
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID
    ): OpResult {

        val result = operation(compId1, compId2, compId3, compId4, compId5)
        if (removeAfterRun)
            FFContext.delete(this)
        return result
    }

    fun <A : Trigger> withTrigger(
        cBuilder: SystemComponentBuilder<A>,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID,
        configure: (A.() -> Unit)): A  {

        val result = super.withTrigger(cBuilder, configure)
        result.call = { invoke(compId1, compId2, compId3, compId4, compId5) }
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