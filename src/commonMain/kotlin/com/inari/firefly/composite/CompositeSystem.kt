package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentBuilder
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType
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
                CREATED       -> CompositeEvent.send(CompositeEventType.CREATED, composite.componentId)
                ACTIVATED     -> activate(composite)
                DEACTIVATED   -> deactivate(composite)
                DELETED       -> CompositeEvent.send(CompositeEventType.DELETED, composite.componentId)
            } }
    )

    init {
        FFContext.loadSystem(this)
    }

    val loadComposite = ComponentRefResolver(Composite) { id ->
        val composite = systemComposites[id]
        composite.systemLoad()
        CompositeEvent.send(
            CompositeEventType.LOADED,
            composite.componentId,
            composite.componentType().subTypeClass)
    }

    val activateComposite = ComponentRefResolver(Composite) { id ->
        FFContext.activate(Composite, id)
    }

    val deactivateComposite = ComponentRefResolver(Composite) { id ->
        FFContext.deactivate(Composite, id)
    }

    val disposeComposite = ComponentRefResolver(Composite) { id ->
        val composite = systemComposites[id]
        composite.systemUnload()

        CompositeEvent.send(
            CompositeEventType.DISPOSED,
            composite.componentId,
            composite.componentType().subTypeClass)
    }

    private fun activate(composite: Composite) {
        composite.systemActivate()

        CompositeEvent.send(
            CompositeEventType.ACTIVATED,
            composite.componentId,
            composite.componentType().subTypeClass)
    }

    private fun deactivate(composite: Composite) {
        composite.systemDeactivate()

        CompositeEvent.send(
            CompositeEventType.DEACTIVATED,
            composite.componentId,
            composite.componentType().subTypeClass)
    }

    fun <C : Composite> getCompositeBuilder(name: String): SystemComponentBuilder<C> {
        @Suppress("UNCHECKED_CAST")
        return compositeBuilderMapping[name] as SystemComponentBuilder<C>
    }

    override fun clearSystem() = systemComposites.clear()
}