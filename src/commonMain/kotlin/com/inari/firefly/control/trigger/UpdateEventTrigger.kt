package com.inari.firefly.control.trigger

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.NULL_CALL
import com.inari.util.Call

class UpdateEventTrigger private constructor() : Trigger() {

    private var call: Call = NULL_CALL
    private val updateEventListener = { doTrigger(call) }

    override fun register(call: Call) {
        this.call = call
        FFContext.registerListener(FFApp.UpdateEvent, updateEventListener)
    }

    override fun dispose() {
        FFContext.disposeListener(FFApp.UpdateEvent, updateEventListener)
        call = NULL_CALL
        super.dispose()
    }

    companion object : Trigger.Subtype<UpdateEventTrigger>() {
        override fun createEmpty() = UpdateEventTrigger()
    }
}