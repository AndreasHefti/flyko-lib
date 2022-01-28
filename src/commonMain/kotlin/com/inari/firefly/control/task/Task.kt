package com.inari.firefly.control.task

import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType
import kotlin.jvm.JvmField

abstract class Task protected constructor() : SystemComponent(Task::class.simpleName!!), TriggeredSystemComponent {

    @JvmField var removeAfterRun: Boolean = false

    abstract operator fun invoke(compId1: Int = -1, compId2: Int = -1, compId3: Int = -1): OpResult

    fun <A : Trigger> withTrigger(
        cBuilder: SystemComponentBuilder<A>,
        compId1: Int = -1,
        compId2: Int = -1,
        compId3: Int = -1,
        configure: (A.() -> Unit)): A  {

        val result = super.withTrigger(cBuilder, configure)
        result.call = { this(compId1, compId2, compId3) }
        return result
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    companion object : SystemComponentType<Task>(Task::class)
}