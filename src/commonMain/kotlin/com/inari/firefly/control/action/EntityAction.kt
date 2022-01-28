package com.inari.firefly.control.action

import com.inari.firefly.control.EMPTY_ENTITY_ACTION
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import kotlin.jvm.JvmField

class EntityAction private constructor() : SystemComponent(EntityAction::class.simpleName!!) {

    @JvmField var call = EMPTY_ENTITY_ACTION
    
    fun invoke(c1: Int = -1, c2: Int = -1, c3: Int = -1) = call(c1, c2, c3)

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<EntityAction>(EntityAction::class) {
        override fun createEmpty() = EntityAction()
    }
}


