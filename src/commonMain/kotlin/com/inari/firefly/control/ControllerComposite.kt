package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.collection.DynIntArray

class ControllerComposite private constructor() : SingleComponentController()  {

    private val controller = DynIntArray(3, -1, 5)

    val withController = ComponentRefResolver(Controller) {
        id -> controller.add(id)
    }

    val withActiveController = ComponentRefResolver(Controller) { id ->
        controller.add(id)
        if (super.controlledComponentId != NO_COMP_ID)
            FFContext[Controller, id].register(componentId)
        FFContext.activate(Controller, id)
    }

    override fun register(componentId: CompId) {
        super.register(componentId)
        val it = controller.iterator()
        while(it.hasNext())
            FFContext[Controller, it.nextInt()].register(componentId)
    }

    override fun unregister(componentId: CompId, disposeWhenEmpty: Boolean) {
        super.unregister(componentId, disposeWhenEmpty)
        val it = controller.iterator()
        while(it.hasNext())
            FFContext[Controller, it.nextInt()].unregister(componentId)
    }

    fun setController(index: Int) = ComponentRefResolver(Controller) { id ->
        controller[index] = id
        if (super.controlledComponentId != NO_COMP_ID)
            FFContext[Controller, id].register(componentId)
    }

    fun setActiveController(index: Int) = ComponentRefResolver(Controller) { id ->
        controller[index] = id
        if (super.controlledComponentId != NO_COMP_ID)
            FFContext[Controller, id].register(componentId)
        FFContext.activate(Controller, id)
    }

    fun removeController(index: Int) = controller.remove(index)
    fun removeAndDeactivateController(index: Int) {
        val id = controller[index]
        if (id >= 0) {
            controller.remove(index)
            if (super.controlledComponentId != NO_COMP_ID)
                FFContext[Controller, id].unregister(componentId)
            FFContext.deactivate(Controller, id)
        }
    }
    fun deactivateController(index: Int) {
        val id = controller[index]
        if (id >= 0) {
            if (super.controlledComponentId != NO_COMP_ID)
                FFContext[Controller, id].unregister(componentId)
            FFContext.deactivate(Controller, id)
        }
    }

    fun <C : Controller> withController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildAndGet(configure)
        controller.add(comp.index)
        if (super.controlledComponentId != NO_COMP_ID)
            FFContext[Controller, comp.index].register(componentId)
        return comp.componentId
    }

    fun <C : Controller> withActiveController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildActivateAndGet(configure)
        controller.add(comp.index)
        if (super.controlledComponentId != NO_COMP_ID)
            FFContext[Controller, comp.index].register(componentId)
        return comp.componentId
    }

    override fun update(componentId: CompId) {
        val it = controller.iterator()
        while(it.hasNext())
            FFContext[Controller, it.nextInt()].update(componentId)
    }

    companion object : SystemComponentSubType<Controller, ControllerComposite>(Controller, ControllerComposite::class) {
        override fun createEmpty() = ControllerComposite()
    }

}