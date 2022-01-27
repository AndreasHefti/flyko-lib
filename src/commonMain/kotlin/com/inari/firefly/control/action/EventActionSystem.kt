package com.inari.firefly.control.action

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects


/** The action system to create and maintain actions of all kind */
object EventActionSystem : ComponentSystem {

    override val supportedComponents: Aspects =
            SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(EntityAction)

    val actions: ComponentMapRO<EntityAction>
        get() = systemActions
    private val systemActions = ComponentSystem.createComponentMapping(
            EntityAction,
            nameMapping = true
    )

    init {
        FFContext.loadSystem(this)
    }

    override fun clearSystem() {
        systemActions.clear()
    }

}