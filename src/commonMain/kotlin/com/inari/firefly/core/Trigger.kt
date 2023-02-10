package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.util.FALSE_SUPPLIER
import com.inari.util.TRUE_SUPPLIER
import kotlin.jvm.JvmField

abstract class Trigger protected constructor(componentType: ComponentType<out Trigger>): Component(componentType) {

    @JvmField var condition: () -> Boolean = FALSE_SUPPLIER
    @JvmField var call: () -> Boolean = TRUE_SUPPLIER

    protected fun doTrigger() {
        if (condition())
            if (!call())
                ComponentSystem.delete(this.key)
    }

    companion object : ComponentSystem<Trigger>("Trigger") {
        override fun allocateArray(size: Int): Array<Trigger?> = arrayOfNulls(size)
        override fun create() = throw UnsupportedOperationException("Trigger is abstract use sub type builder instead")
    }
}

interface TriggeredComponent {

    fun <A : Trigger> withTrigger(builder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val key = builder(configure)
        ComponentSystem.activate(key)
        return key
    }

}

class UpdateEventTrigger private constructor() : Trigger(UpdateEventTrigger) {

    private val updateEventListener = { doTrigger() }

    override fun load() = Engine.registerListener(UPDATE_EVENT_TYPE, updateEventListener)
    override fun dispose() = Engine.disposeListener(UPDATE_EVENT_TYPE, updateEventListener)

    companion object : ComponentSubTypeBuilder<Trigger, UpdateEventTrigger>(Trigger,"UpdateEventTrigger") {
        override fun create() = UpdateEventTrigger()
    }
}