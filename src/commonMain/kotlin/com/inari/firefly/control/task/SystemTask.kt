package com.inari.firefly.control.task

import com.inari.firefly.NULL_CALL
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.Call

class SystemTask private constructor() : TriggeredSystemComponent(SystemTask::class.simpleName!!) {

    private val triggerCall = { task() }

    var removeAfterRun: Boolean = false
    var task: Call = NULL_CALL

    fun <A : Trigger> trigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, triggerCall, configure)

    override fun componentType(): ComponentType<SystemTask> = Companion
    companion object : SystemComponentSingleType<SystemTask>(SystemTask::class) {
        override fun createEmpty() = SystemTask()
    }
}