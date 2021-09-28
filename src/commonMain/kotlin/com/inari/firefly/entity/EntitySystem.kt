package com.inari.firefly.entity

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects

object EntitySystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Entity)

    val entities: ComponentMapRO<Entity>
        get() = systemEntities
    private val systemEntities: ComponentMap<Entity> = ComponentSystem.createComponentMapping(
        Entity,
        activationMapping = true,
        nameMapping = true,
        listener = { entity, action -> when (action) {
            CREATED       -> EntityEvent.send(entity, EntityEventType.ON_CREATE)
            ACTIVATED     -> EntityEvent.send(entity, EntityEventType.ACTIVATED)
            DEACTIVATED   ->  EntityEvent.send(entity, EntityEventType.DEACTIVATED)
            DELETED       ->  EntityEvent.send(entity, EntityEventType.ON_DISPOSE)
        } }
    )

    init {
        FFContext.loadSystem(this)
    }

    operator fun get(entityId: CompId) = systemEntities[entityId.instanceId]
    operator fun get(name: String) = systemEntities[name]
    operator fun get(index: Int) = systemEntities[index]
    operator fun contains(entityId: CompId) = entityId in systemEntities
    operator fun contains(name: String) = name in systemEntities
    operator fun contains(index: Int) = index in systemEntities

    override fun clearSystem() = systemEntities.clear()

}