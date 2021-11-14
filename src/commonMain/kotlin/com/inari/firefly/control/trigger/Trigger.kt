package com.inari.firefly.control.trigger

import com.inari.firefly.*
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.Call
import com.inari.util.aspect.AspectType
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

@ComponentDSL
abstract class Trigger protected constructor() : SystemComponent(Trigger::class.simpleName!!) {

    @JvmField internal var triggeredComponentId = NO_COMP_ID

    @JvmField var disposeAfter: Boolean = false
    @JvmField var condition: () -> Boolean = TRUE_SUPPLIER
    @JvmField var call: Call = NULL_CALL

    protected fun doTrigger() {
        if (call != NULL_CALL && condition()) {
            call()
            if (disposeAfter)
                dispose()
        }
    }

    companion object : SystemComponentType<Trigger>(Trigger::class)
}