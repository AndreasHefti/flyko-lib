package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType

abstract class ComponentTask protected constructor() : SystemComponent(ComponentTask::class.simpleName!!), TriggeredSystemComponent {

    var removeAfterRun: Boolean = false

    fun invoke(
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID) {

        task(compId1, compId2, compId3, compId4, compId5)
        if (removeAfterRun)
            FFContext.delete(this)
    }

    abstract fun task(
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID)

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
    companion object : SystemComponentType<ComponentTask>(ComponentTask::class)

}