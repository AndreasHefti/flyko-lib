package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.Named

interface ControlledSystemComponent {

    val componentId: CompId

    fun withController(id: CompId) { ControllerSystem.registerMapping(id.index, componentId) }
    fun withController(name: String) = { ControllerSystem.registerMapping(FFContext[Controller, name].index, componentId) }
    fun withController(named: Named) = { ControllerSystem.registerMapping(FFContext[Controller, named].index, componentId) }
    fun withController(component: Controller) = { ControllerSystem.registerMapping(component.index, componentId) }

    fun <C : Controller> withController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val compId = cBuilder.build(configure)
        ControllerSystem.registerMapping(compId.index, componentId)
        return compId
    }

    fun <C : Controller> withActiveController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val compId = cBuilder.build(configure)
        ControllerSystem.registerMapping(compId.index, componentId)
        FFContext.activate(compId)
        return compId
    }

    fun disposeController() = ControllerSystem.dispsoeMappingsFor(componentId)


}
