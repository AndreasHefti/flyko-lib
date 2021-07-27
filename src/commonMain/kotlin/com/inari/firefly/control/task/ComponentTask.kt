package com.inari.firefly.control.task

import com.inari.firefly.EMPTY_COMPONENT_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.ComponentTaskOperation
import com.inari.util.OpResult

class ComponentTask private constructor() : SystemComponent(ComponentTask::class.simpleName!!), TriggeredSystemComponent {

    var removeAfterRun: Boolean = false
    var operation: ComponentTaskOperation = EMPTY_COMPONENT_TASK_OPERATION

    fun invoke(
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult {

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
    companion object : SystemComponentSingleType<ComponentTask>(ComponentTask::class) {
        override fun createEmpty() = ComponentTask()
    }

}