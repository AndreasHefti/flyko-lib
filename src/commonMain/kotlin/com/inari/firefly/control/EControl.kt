package com.inari.firefly.control

import com.inari.firefly.ENTITY_CONTROL_ASPECT_GROUP
import com.inari.firefly.FFContext
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

class EControl private constructor() : EntityComponent(EControl::class.simpleName!!) {

    @JvmField val aspects: Aspects = ENTITY_CONTROL_ASPECT_GROUP.createAspects()
    @JvmField val withController = ComponentRefResolver(Controller) { index->
        ControllerSystem.registerMapping(index, entityId)
    }

    fun <A : Controller> withController(builder: SystemComponentSubType<Controller, A>, configure: (A.() -> Unit)): CompId {
        val compId = builder.build(configure)
        ControllerSystem.registerMapping(compId.index, entityId)
        return compId
    }

    fun disposeController() = ControllerSystem.dispsoeMappingsFor(entityId)

    override fun reset() {
        aspects.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EControl>(EControl::class) {
        override fun createEmpty() = EControl()
    }
}