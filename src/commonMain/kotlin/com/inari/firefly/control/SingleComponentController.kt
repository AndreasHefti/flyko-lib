package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId

abstract class SingleComponentController protected constructor() : Controller() {

    var controlledComponentId = NO_COMP_ID

    override fun update() {
        if (controlledComponentId != NO_COMP_ID)
            update(controlledComponentId)
    }

    override fun register(componentId: CompId) {
        controlledComponentId = componentId
    }
    override fun unregister(componentId: CompId, disposeWhenEmpty: Boolean) {
        controlledComponentId = NO_COMP_ID
        if (disposeWhenEmpty)
            FFContext.delete(componentId)
    }

}