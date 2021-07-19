package com.inari.firefly.control

import com.inari.firefly.ENTITY_CONTROL_ASPECT_GROUP
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

class EControl private constructor() : EntityComponent(EControl::class.simpleName!!) {

    @JvmField val aspects: Aspects = ENTITY_CONTROL_ASPECT_GROUP.createAspects()

    fun <C : Controller> withController(cBuilder: SystemComponentBuilder<C>, configure: (C.() -> Unit)): CompId {
        val comp = cBuilder.buildAndGet(configure)
        comp.controlled.set(this.index)
        return comp.componentId
    }

    override fun reset() {
        aspects.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EControl>(EControl::class) {
        override fun createEmpty() = EControl()
    }
}