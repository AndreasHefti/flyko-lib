package com.inari.firefly.control.action

import com.inari.firefly.FFContext
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

typealias ActionCall = (Int, Int, Int, Int) -> Unit
object ActionSystem : ComponentSystem {

    override val supportedComponents: Aspects =
            SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Action)

    val actions: ComponentMapRO<Action>
        get() = systemActions
    private val systemActions = ComponentSystem.createComponentMapping(
            Action,
            nameMapping = true
    )

    init {
        FFContext.loadSystem(this)
    }

    override fun clearSystem() {
        systemActions.clear()
    }

}