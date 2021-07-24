package com.inari.firefly.core.api

import com.inari.util.collection.DynArray

abstract class TimerAPI {

    abstract val time: Long
    abstract val timeElapsed: Long
    abstract val tickAction: () -> Unit

    private val schedulers: DynArray<FFTimer.UpdateScheduler> = DynArray.of( 20)

    internal fun updateSchedulers() {
        var i = 0
        val to = schedulers.capacity
        while ( i < to) {
            val updateScheduler = schedulers[i++] ?: continue
            updateScheduler.update()
        }
    }

    fun createUpdateScheduler(resolution: Float): FFTimer.UpdateScheduler {
        val updateScheduler = FFTimer.UpdateScheduler(resolution)
        schedulers.add(updateScheduler)
        return updateScheduler
    }

    internal fun tick() {
        tickAction()
    }

}