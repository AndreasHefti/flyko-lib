package com.inari.firefly.control.action

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import kotlin.jvm.JvmField

class Action private constructor() : SystemComponent(Action::class.simpleName!!) {

    @JvmField var call: ActionCall = { _, _, _, _ -> }

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


