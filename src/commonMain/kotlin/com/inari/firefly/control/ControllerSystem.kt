package com.inari.firefly.control

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.util.aspect.Aspects

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
    private val entityActivationListener: EntityEventListener = object: EntityEventListener {
        override fun entityActivated(entity: Entity) {
            val control = entity[EControl]
            if (!control.controller.isEmpty) {
                var i = control.controller.nextSetBit(0)
                while (i >= 0) {
                    ControllerSystem.controller[i].register(entity.componentId)
                    i = control.controller.nextSetBit(i + 1)
                }
            }
        }
        override fun entityDeactivated(entity: Entity) = unregister(entity.componentId)
        override fun match(aspects: Aspects): Boolean = EControl in aspects
    }

    init {
        FFContext.registerListener(FFApp.UpdateEvent, updateListener)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.loadSystem(this)
    }

    internal fun register(componentId: CompId) =
        controller.forEachActive { c -> c.register(componentId) }

    internal fun unregister(componentId: CompId, disposeWhenEmpty: Boolean = false) =
        controller.forEachActive { c -> c.unregister(componentId, disposeWhenEmpty) }


    override fun clearSystem() {
        controller.clear()
    }

}