package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class Controller protected constructor() : SystemComponent("Controller") {

    internal val controlled: BitSet = BitSet()

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    abstract fun update(componentId: Int)

    override fun componentType() = Companion
    companion object : SystemComponentType<Controller>(Controller::class)
}