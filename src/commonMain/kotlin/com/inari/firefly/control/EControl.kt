package com.inari.firefly.control

import com.inari.firefly.ENTITY_CONTROL_ASPECT_GROUP
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

class EControl private constructor() : EntityComponent(EControl::class.simpleName!!) {

    @JvmField val aspects: Aspects = ENTITY_CONTROL_ASPECT_GROUP.createAspects()

    val controller = ComponentRefResolver(Controller) { index->
        ControllerSystem.controller[index].register(entityId)
    }

    fun <A : Controller> withController(builder: SystemComponentSubType<Controller, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EMovement instance is already created")
        val comp = builder.buildAndGet(configure)
        comp.register(entityId)
        return comp.componentId
    }

    fun <A : Controller> withActiveController(builder: SystemComponentSubType<Controller, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EMovement instance is already created")
        val comp = builder.buildActivateAndGet(configure)
        comp.register(entityId)
        return comp.componentId
    }

    override fun reset() {
        ControllerSystem.unregister(entityId, true)
        aspects.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EControl>(EControl::class) {
        override fun createEmpty() = EControl()
    }
}