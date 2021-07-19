package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.collection.DynIntArray

class ControllerComposite private constructor() : Controller()  {

    private val controller = DynIntArray(3, -1, 5)

    val withController = ComponentRefResolver(Controller) {
        id -> controller.add(id)
    }

    val withActiveController = ComponentRefResolver(Controller) { id ->
        controller.add(id)
        FFContext.activate(Controller, id)
    }

    fun setController(index: Int) = ComponentRefResolver(Controller) {
            id -> controller[index] = id
    }

    fun setActiveController(index: Int) = ComponentRefResolver(Controller) { id ->
        controller[index] = id
        FFContext.activate(Controller, id)
    }

    fun removeController(index: Int) = controller.remove(index)
    fun removeAndDeactivateController(index: Int) {
        val id = controller[index]
        if (id >= 0) {
            controller.remove(index)
            FFContext.deactivate(Controller, id)
        }
    }
    fun deactivateController(index: Int) {
        val id = controller[index]
        if (id >= 0) {
            FFContext.deactivate(Controller, id)
        }
    }

    fun <C : Controller> withController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildAndGet(configure)
        comp.controlled.set(this.index)
        controller.add(comp.index)
        return comp.componentId
    }

    fun <C : Controller> withActiveController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildActivateAndGet(configure)
        comp.controlled.set(this.index)
        controller.add(comp.index)
        return comp.componentId
    }

    override fun update(componentId: Int) {
        val it = controller.iterator()
        while(it.hasNext())
            FFContext[Controller, it.nextInt()].update(componentId)
    }

    companion object : SystemComponentSubType<Controller, ControllerComposite>(Controller, ControllerComposite::class) {
        override fun createEmpty() = ControllerComposite()
    }
}