package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.Named

interface ControlledSystemComponent {

    val componentId: CompId

    fun withController(id: CompId) { FFContext[Controller, id].register(componentId) }
    fun withController(name: String) = { FFContext[Controller, name].register(componentId) }
    fun withController(named: Named) = { FFContext[Controller, named].register(componentId) }
    fun withController(component: Controller) = { component.register(componentId) }

    fun <C : Controller> withController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildAndGet(configure)
        comp.register(componentId)
        return comp.componentId
    }

    fun <C : Controller> withActiveController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildActivateAndGet(configure)
        comp.register(componentId)
        return comp.componentId
    }

}
