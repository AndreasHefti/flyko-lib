package com.inari.firefly.control.trigger

import com.inari.firefly.*
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.BooleanSupplier
import com.inari.util.Call
import com.inari.util.aspect.AspectType
import kotlin.reflect.KClass

@ComponentDSL
abstract class Trigger protected constructor() : SystemComponent(Trigger::class.simpleName!!) {

    var disposeAfter: Boolean = false
    var condition: BooleanSupplier  = TRUE_SUPPLIER

    abstract fun register(call: Call)

    protected fun doTrigger(call: Call) {
        if (condition()) {
            call()
            if (disposeAfter)
                dispose()
        }
    }

    override fun componentType(): ComponentType<Trigger> = Companion
    companion object : SystemComponentType<Trigger>(Trigger::class)

    abstract class Subtype<A : Trigger> : ComponentType<A> {
        internal fun doBuild(configure: A.() -> Unit): A {
            val result = createEmpty()
            result.also(configure)
            TriggerSystem.trigger.receiver()(result)
            return result
        }
        fun build(call: Call, configure: A.() -> Unit): Int {
            val result = doBuild(configure)
            result.register(call)
            return result.index
        }
        override val typeClass: KClass<out Component> = Trigger.typeClass
        final override val aspectIndex: Int = Trigger.aspectIndex
        final override val aspectName: String = Trigger.aspectName
        final override val aspectType: AspectType = Trigger.aspectType
        protected abstract fun createEmpty(): A
    }
}