package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object CompositeSystem : ComponentSystem {

    override val supportedComponents: Aspects =
            SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Composite)

    @JvmField val composites = ComponentSystem.createComponentMapping(
            Composite,
            activationMapping = true,
            nameMapping = true,
            listener = { composite, action  -> when (action) {
                CREATED       -> composite.systemLoad()
                ACTIVATED     -> composite.systemActivate()
                DEACTIVATED   -> composite.systemDeactivate()
                DELETED       -> composite.systemUnload()
            } }
    )

    init {
        FFContext.loadSystem(this)
    }

    override fun clearSystem() = composites.clear()

}