package com.inari.firefly.control.trigger

import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object TriggerSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Trigger)

    @JvmField val trigger = ComponentSystem.createComponentMapping(
        Trigger,
        nameMapping = true
    )

    override fun clearSystem() {
        trigger.clear()
    }
}