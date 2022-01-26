package com.inari.firefly.control.action

import com.inari.firefly.control.Consideration
import com.inari.firefly.control.EMPTY_ENTITY_ACTION_CALL
import com.inari.firefly.control.EntityActionCall
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class Action private constructor() : SystemComponent(Action::class.simpleName!!) {

    @JvmField var call = EMPTY_ENTITY_ACTION_CALL
    @JvmField val considerations = DynArray.of<Consideration>(1, 1)

    fun invoke(
            entity1: Int = -1,
            entity2: Int = -1,
            entity3: Int = -1,
            entity4: Int = -1) {

        call(entity1, entity2, entity3, entity4)
    }

    override fun componentType() = Companion
    companion object : SystemComponentSingleType<Action>(Action::class) {
        override fun createEmpty() = Action()
    }
}


