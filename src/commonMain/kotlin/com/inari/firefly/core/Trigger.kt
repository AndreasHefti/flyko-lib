package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.api.VOID_ACTION
import kotlin.jvm.JvmField

abstract class Trigger protected constructor(): Component(Trigger) {

    @JvmField var componentRef = NO_COMPONENT_KEY
    @JvmField var conditionRef = CReference(ConditionalComponent)
    @JvmField var action = VOID_ACTION
    @JvmField var deleteAfterFired = false

    fun withCondition(configure: (Conditional.() -> Unit)): ComponentKey {
        val key = Conditional.build(configure)
        conditionRef(key)
        return key
    }

    protected fun checkTrigger() {
        if (conditionRef.exists && ConditionalComponent[conditionRef](componentRef)) {
            action(componentRef)
            if (deleteAfterFired)
                Trigger.delete(this)
        }
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

open class UpdateEventTrigger private constructor() : Trigger() {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }

    private fun update() {
        if (scheduler.needsUpdate())
            checkTrigger()
    }

    override fun load() = Engine.registerListener(UPDATE_EVENT_TYPE, ::update)
    override fun dispose() = Engine.disposeListener(UPDATE_EVENT_TYPE, ::update)

    companion object : SubComponentBuilder<Trigger, UpdateEventTrigger>(Trigger) {
        override fun create() = UpdateEventTrigger()
    }
}

class InputButtonTrigger private constructor() : Trigger() {

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var triggerButton = ButtonType.ENTER

    private fun update() {
        if (inputDevice.buttonPressed(triggerButton))
            checkTrigger()
    }

    override fun load() = Engine.registerListener(UPDATE_EVENT_TYPE, ::update)
    override fun dispose() = Engine.disposeListener(UPDATE_EVENT_TYPE, ::update)

    companion object : SubComponentBuilder<Trigger, InputButtonTrigger>(Trigger) {
        override fun create() = InputButtonTrigger()
    }
}