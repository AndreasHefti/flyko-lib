package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.util.collection.DynArray

abstract class CollectionController protected constructor() : Controller() {

    protected val controlledComponentIds: DynArray<CompId> = DynArray.of(5, 5)

    override fun update() =
        controlledComponentIds.forEach { id -> this.update(id) }

    override fun register(componentId: CompId) {
        controlledComponentIds + componentId
    }
    override fun unregister(componentId: CompId, disposeWhenEmpty: Boolean) {
        controlledComponentIds - componentId
        if (disposeWhenEmpty && controlledComponentIds.isEmpty)
            FFContext.delete(componentId)
    }
}