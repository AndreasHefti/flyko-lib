package com.inari.firefly.control

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object ControllerSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        Controller
    )

    @JvmField val controller = ComponentSystem.createComponentMapping(
        Controller,
        activationMapping = true,
        nameMapping = true
    )

    init {
        FFContext.registerListener(FFApp.UpdateEvent) {
            controller.forEachActive { c ->
                var i: Int = c.controlled.nextSetBit(0)
                while (i >= 0) {
                    c.update(i)
                    i = c.controlled.nextSetBit(i + 1)
                }
            }
        }
        FFContext.loadSystem(this)
    }

    override fun clearSystem() {
        controller.clear()
    }
}