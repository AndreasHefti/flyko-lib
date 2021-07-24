package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType

abstract class GenericTask protected constructor() : SystemComponent(GenericTask::class.simpleName!!), TriggeredSystemComponent {

    private val triggerCall = { invoke() }

    var removeAfterRun: Boolean = false

    fun invoke() {
        task()
        if (removeAfterRun)
            FFContext.delete(this)
    }

    abstract fun task()

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
    companion object : SystemComponentType<GenericTask>(GenericTask::class)

}