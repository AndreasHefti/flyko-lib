package com.inari.firefly.control.behavior

import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

abstract class BxBranch internal constructor() : BxNode() {

    @JvmField internal val children: DynArray<BxNode> = DynArray.of(5, 5)

    fun <C : BxNode> node(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val id = cBuilder.build(configure)
        children.add(BehaviorSystem.nodes[id.instanceId])
        return id
    }
}