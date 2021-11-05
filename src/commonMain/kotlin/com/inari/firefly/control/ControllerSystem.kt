package com.inari.firefly.control

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray

object ControllerSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        Controller)

    val controller: ComponentMapRO<Controller>
        get() = systemController
    private val systemController = ComponentSystem.createComponentMapping(
        Controller,
        activationMapping = true,
        nameMapping = true,
        listener = { controller, action  -> when (action) {
            ComponentMap.MapAction.ACTIVATED -> activateCompIds(controller)
            ComponentMap.MapAction.DEACTIVATED -> deactivateCompIds(controller)
            else -> DO_NOTHING
        } }
    )

    private val updateListener = {
        activeMappings.forEach { mapping ->
            if (mapping.controller.scheduler.needsUpdate())
                mapping.controller.update(mapping.componentId)
        }
    }

    private val entityActivationListener: EntityEventListener = object: EntityEventListener {
        override fun entityActivated(entity: Entity) = activatForComponent(entity.componentId)
        override fun entityDeactivated(entity: Entity) = deactivatForComponent(entity.componentId)
        override fun match(aspects: Aspects): Boolean = EControl in aspects
    }

    private val mappings = DynArray.of<ControllerMapping>(30, 50)
    private val activeMappings = DynArray.of<ControllerMapping>(30, 50)

    init {
        FFContext.registerListener(FFApp.UpdateEvent, updateListener)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.loadSystem(this)
    }

    private fun activatForComponent(componentId: CompId) {
        mappings.forEach { mapping ->
            if (mapping.componentId == componentId) {
                mapping.controller.init(componentId)
                activeMappings + mapping
            }
        }
    }

    private fun deactivatForComponent(componentId: CompId) {
        mappings.forEach { mapping ->
            if (mapping.componentId == componentId)
                activeMappings - mapping
        }
    }

    private fun activateCompIds(controller: Controller) {
        mappings.forEach { mapping ->
            if (mapping.controller == controller) {
                controller.init(mapping.componentId)
                activeMappings + mapping
            }
        }
    }

    private fun deactivateCompIds(controller: Controller) {
        mappings.forEach { mapping ->
            if (mapping.controller == controller)
                activeMappings - mapping
        }
    }

    internal fun registerMapping(controllerId: Int, componentId: CompId) {
        val mapping = ControllerMapping(controller[controllerId], componentId)
        mappings + mapping
        if (controller.isActive(controllerId)) {
            mapping.controller.init(mapping.componentId)
            activeMappings + mapping
        }
    }

    internal fun dispsoeMappingsFor(componentId: CompId) {
        mappings
            .filter { it.componentId == componentId }
            .forEach { mapping ->
                mappings - mapping
                if (controller.isActive(mapping.controller.index))
                    activeMappings - mapping
            }
    }

    override fun clearSystem() {
        activeMappings.clear()
        mappings.clear()
        systemController.clear()
    }

    private class ControllerMapping(
        internal val controller: Controller,
        internal val componentId: CompId
    )

}