package com.inari.firefly.entity

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.EntityActivationEvent.Type.ACTIVATED
import com.inari.firefly.entity.EntityActivationEvent.Type.DEACTIVATED
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object EntitySystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Entity)

    @JvmField val entities: ComponentMap<Entity> = ComponentSystem.createComponentMapping(
        Entity,
        activationMapping = true,
        nameMapping = true,
        listener = { entity, action -> when (action) {
            ComponentMap.MapAction.ACTIVATED     -> activated(entity)
            ComponentMap.MapAction.DEACTIVATED   -> deactivated(entity)
            else -> {}
        } }
    )

    init {
        FFContext.loadSystem(this)
    }

    operator fun get(entityId: CompId) = entities[entityId.instanceId]
    operator fun get(name: String) = entities[name]
    operator fun get(index: Int) = entities[index]
    operator fun contains(entityId: CompId) = entityId in entities
    operator fun contains(name: String) = name in entities
    operator fun contains(index: Int) = index in entities

    private fun activated(entity: Entity) {
        EntityActivationEvent.send(
            entity = entity,
            type = ACTIVATED
        )
    }

    private fun deactivated(entity: Entity) {
        EntityActivationEvent.send(
            entity = entity,
            type = DEACTIVATED
        )
    }

    override fun clearSystem() = entities.clear()

}