package com.inari.firefly.control.trigger

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.rendering.Renderer

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

    override fun componentType() = Renderer
    companion object : SystemComponentSubType<Trigger, UpdateEventTrigger>(Trigger, UpdateEventTrigger::class) {
        override fun createEmpty() = UpdateEventTrigger()
    }
}