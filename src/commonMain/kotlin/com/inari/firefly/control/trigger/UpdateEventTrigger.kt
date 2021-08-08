package com.inari.firefly.control.trigger

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SystemComponentSubType

class UpdateEventTrigger private constructor() : Trigger() {

    private val updateEventListener = { doTrigger() }

    override fun init() {
        super.init()
        FFContext.registerListener(FFApp.UpdateEvent, updateEventListener)
    }

    override fun dispose() {
        FFContext.disposeListener(FFApp.UpdateEvent, updateEventListener)
        super.dispose()
    }

    companion object : SystemComponentSubType<Trigger, UpdateEventTrigger>(Trigger, UpdateEventTrigger::class) {
        override fun createEmpty() = UpdateEventTrigger()
    }
}