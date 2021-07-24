package com.inari.firefly.control.trigger

import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder

interface TriggeredSystemComponent {

    val componentId: CompId

    fun <A : Trigger> withTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = cBuilder.buildAndGet(configure)
        result.triggeredComponentId = componentId
        return result
    }

    fun disposeTrigger() {
        TriggerSystem.disposeForComponent(componentId)
    }
}