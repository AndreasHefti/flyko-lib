package com.inari.firefly.control.trigger

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object TriggerSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Trigger)

    val trigger: ComponentMapRO<Trigger>
        get() = systemTrigger
    @JvmField internal val systemTrigger = ComponentSystem.createComponentMapping(
        Trigger,
        nameMapping = true
    )

    fun disposeForComponent(componentId: CompId) {
        systemTrigger.forEach { trigger ->
            if (trigger.triggeredComponentId == componentId)
                FFContext.delete(trigger)
        }
    }

    override fun clearSystem() {
        systemTrigger.clear()
    }
}