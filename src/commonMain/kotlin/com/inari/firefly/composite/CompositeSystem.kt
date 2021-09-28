package com.inari.firefly.composite

import com.inari.firefly.FFContext

import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefPredicate
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object CompositeSystem : ComponentSystem {

    override val supportedComponents: Aspects =
            SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Composite)

    @JvmField internal val compositeBuilderMapping: MutableMap<String, SystemComponentBuilder<Composite>> = mutableMapOf()

    val composites: ComponentMapRO<Composite>
        get() = systemComposites
    private val systemComposites = ComponentSystem.createComponentMapping(
            Composite,
            activationMapping = true,
            nameMapping = true,
            listener = { composite, action  -> when (action) {
                CREATED       ->    CompositeEvent.send(CompositeEventType.CREATED, composite.componentId, composite.componentType())
                ACTIVATED     ->    activate(composite)
                DEACTIVATED   ->    deactivate(composite)
                DELETED       ->    CompositeEvent.send(CompositeEventType.DELETED,composite.componentId, composite.componentType())
            } }
    )

    val loaderDispatcher = ComponentSystem.createLoaderDispatcher(
        Composite,
        ComponentRefResolver(Composite) {
            with(systemComposites[it]) {
                systemLoad()
                CompositeEvent.send(
                    CompositeEventType.LOADED,
                    componentId,
                    componentType()
                )
            }
        },
        ComponentRefPredicate(Composite) { systemComposites[it].loaded },
        ComponentRefResolver(Composite) {
            with(systemComposites[it]) {
                systemDispose()
                CompositeEvent.send(
                    CompositeEventType.DISPOSED,
                    componentId,
                    componentType()
                )
            }
        }
    )

    private val entityListener: EntityEventListener = object : EntityEventListener {
        override fun entityOnCreation(entity: Entity) = loadEntityComposite(entity)
        override fun entityActivated(entity: Entity) = activateEntityComposite(entity)
        override fun entityDeactivated(entity: Entity) = deactivateEntityComposite(entity)
        override fun entityOnDispose(entity: Entity) = disposeEntityComposite(entity)
        override fun match(aspects: Aspects): Boolean = EComposite in aspects
    }

    init {
        FFContext.loadSystem(this)
        FFContext.registerListener(EntityEvent, entityListener)
    }

    private fun activate(composite: Composite) {
        composite.systemActivate()

        CompositeEvent.send(
            CompositeEventType.ACTIVATED,
            composite.componentId,
            composite.componentType())
    }

    private fun deactivate(composite: Composite) {
        composite.systemDeactivate()

        CompositeEvent.send(
            CompositeEventType.DEACTIVATED,
            composite.componentId,
            composite.componentType())
    }

    private fun loadEntityComposite(entity: Entity) = with(entity[EComposite]) {
        if (loadTaskRef >= 0)
            TaskSystem.runTask(loadTaskRef, entity.componentId)
    }

    private fun activateEntityComposite(entity: Entity) = with(entity[EComposite]) {
        if (activationTaskRef >= 0)
            TaskSystem.runTask(activationTaskRef, entity.componentId)
    }

    private fun deactivateEntityComposite(entity: Entity) = with(entity[EComposite]) {
        if (deactivationTaskRef >= 0)
            TaskSystem.runTask(deactivationTaskRef, entity.componentId)
    }

    private fun disposeEntityComposite(entity: Entity) = with(entity[EComposite]) {
        if (disposeTaskRef >= 0)
            TaskSystem.runTask(disposeTaskRef, entity.componentId)
    }

    fun <C : Composite> getCompositeBuilder(name: String): SystemComponentBuilder<C> {
        @Suppress("UNCHECKED_CAST")
        return compositeBuilderMapping[name] as SystemComponentBuilder<C>
    }

    override fun clearSystem() {
        FFContext.disposeListener(EntityEvent, entityListener)
        systemComposites.clear()
    }
}