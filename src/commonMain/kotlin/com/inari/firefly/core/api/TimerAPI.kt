package com.inari.firefly.core.api

import com.inari.util.collection.DynArray
import com.inari.util.timeMillis
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object FFTimer : TimerAPI() {

    private var lastUpdateTime: Long = 0

    override var time: Long = 0
        private set
    override var timeElapsed: Long = 0
        private set


    private val schedulers: DynArray<UpdateScheduler> = DynArray.of( 20)

    override val tickAction: () -> Unit
        get() = {
            if (lastUpdateTime == 0L) {
                lastUpdateTime = timeMillis()
            } else {
                val currentTime = timeMillis()
                time += timeElapsed
                timeElapsed = currentTime - lastUpdateTime
                lastUpdateTime = currentTime
            }
        }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("FFTimer [lastUpdateTime=")
        builder.append(lastUpdateTime)
        builder.append(", time=")
        builder.append(time)
        builder.append(", timeElapsed=")
        builder.append(timeElapsed)
        builder.append("]")
        return builder.toString()
    }

    interface Scheduler {
        val resolution: Float
        fun needsUpdate(): Boolean
    }

    class UpdateScheduler internal constructor(override val resolution: Float) : Scheduler {
        private val delayMillis: Long = (1000 / resolution).toLong()
        private var lastUpdate: Long = -1
        var tick: Long = 0
            private set
        private var needsUpdate: Boolean = false
        private var updated: Boolean = false

        internal fun update() {
            if (lastUpdateTime - lastUpdate >= delayMillis) {
                lastUpdate = lastUpdateTime
                tick++
                needsUpdate = true
            } else if (updated) {
                needsUpdate = false
                updated = false
            }
        }

        override fun needsUpdate(): Boolean {
            if (needsUpdate) {
                updated = true
            }
            return needsUpdate
        }

        fun reset() {
            lastUpdate = -1
            tick = 0
        }
    }
}

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