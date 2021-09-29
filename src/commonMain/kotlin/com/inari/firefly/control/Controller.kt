package com.inari.firefly.control

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NULL_INT_CONSUMER
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.IntConsumer
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

abstract class Controller protected constructor() : SystemComponent(Controller::class.simpleName!!) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    abstract fun init(componentId: CompId)
    abstract fun update(componentId: CompId)

    override fun componentType() = Companion
    companion object : SystemComponentType<Controller>(Controller::class)

}