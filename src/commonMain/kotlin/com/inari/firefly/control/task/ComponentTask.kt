package com.inari.firefly.control.task

import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.OpResult
import kotlin.jvm.JvmField

abstract class ComponentTask protected constructor() : SystemComponent(ComponentTask::class.simpleName!!), TriggeredSystemComponent {

    @JvmField var removeAfterRun: Boolean = false

    abstract operator fun invoke(
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult

    fun <A : Trigger> withTrigger(
        cBuilder: SystemComponentBuilder<A>,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID,
        configure: (A.() -> Unit)): A  {

        val result = super.withTrigger(cBuilder, configure)
        result.call = { this(compId1, compId2, compId3, compId4, compId5) }
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType() = Companion
    companion object : SystemComponentType<ComponentTask>(ComponentTask::class)

}