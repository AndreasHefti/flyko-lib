package com.inari.firefly.core

import com.inari.util.FALSE_SUPPLIER
import com.inari.util.TRUE_SUPPLIER
import kotlin.jvm.JvmField


abstract class Trigger protected constructor(): Component(Trigger) {

    @JvmField var condition: () -> Boolean = FALSE_SUPPLIER
    @JvmField var call: () -> Boolean = TRUE_SUPPLIER

    protected fun doTrigger() {
        if (condition())
            if (!call())
                Trigger.delete(this)
    }

    override val componentType = Companion
    companion object : ComponentSystem<Trigger>("Trigger") {
        override fun allocateArray(size: Int): Array<Trigger?> = arrayOfNulls(size)
        override fun create(): Trigger =
            throw UnsupportedOperationException("Trigger is abstract use a concrete implementation instead")
    }
}

interface TriggeredComponent {

    fun <A : Trigger> withTrigger(builder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        return builder.buildActive(configure)
    }

}

class UpdateEventTrigger private constructor() : Trigger() {

    private val updateEventListener = { doTrigger() }

    override fun load() = Engine.registerListener(Engine.UpdateEvent, updateEventListener)
    override fun dispose() = Engine.disposeListener(Engine.UpdateEvent, updateEventListener)

    companion object :  ComponentSubTypeSystem<Trigger, UpdateEventTrigger>(Trigger) {
        override fun create() = UpdateEventTrigger()
    }
}