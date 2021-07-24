package com.inari.firefly.control

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object ControllerSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        Controller,
    )

    internal val controller = ComponentSystem.createComponentMapping(
        Controller,
        activationMapping = true,
        nameMapping = true
    )

    private val updateListener = {
        controller.forEachActive(Controller::update)
    }

    init {
        FFContext.registerListener(FFApp.UpdateEvent, updateListener)
    }

    internal fun unregister(componentId: CompId, disposeWhenEmpty: Boolean = false) =
        controller.forEachActive { c -> c.unregister(componentId, disposeWhenEmpty) }


    override fun clearSystem() {
        controller.clear()
    }

}