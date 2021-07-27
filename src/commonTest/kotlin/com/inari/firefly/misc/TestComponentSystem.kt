package com.inari.firefly.misc

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent.Companion.SYSTEM_COMPONENT_ASPECTS
import com.inari.util.aspect.Aspects

object TestComponentSystem : ComponentSystem {

    override val supportedComponents: Aspects = SYSTEM_COMPONENT_ASPECTS.createAspects(
        TestComponent,
        Test2Component
    )
    private val c1: ComponentMap<TestComponent> = ComponentSystem.createComponentMapping(TestComponent)
    private val c2: ComponentMap<Test2Component> = ComponentSystem.createComponentMapping(Test2Component)

    init {

        FFContext.registerListener(
            StaticTestEvent,
            {id: CompId ->  println("event: $id") }
        )
    }

    override fun clearSystem() {
        c1.clear()
        c2.clear()
    }

}