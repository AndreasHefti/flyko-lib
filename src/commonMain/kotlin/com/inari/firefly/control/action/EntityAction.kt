package com.inari.firefly.control.action

import com.inari.firefly.control.EMPTY_ENTITY_ACTION_CALL
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import kotlin.jvm.JvmField

class EntityAction private constructor() : SystemComponent(EntityAction::class.simpleName!!) {

    @JvmField var call = EMPTY_ENTITY_ACTION_CALL
    fun invoke(
            entity1: Int = -1,
            entity2: Int = -1,
            entity3: Int = -1,
            entity4: Int = -1) {

        call(entity1, entity2, entity3, entity4)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<EntityAction>(EntityAction::class) {
        override fun createEmpty() = EntityAction()
    }
}


