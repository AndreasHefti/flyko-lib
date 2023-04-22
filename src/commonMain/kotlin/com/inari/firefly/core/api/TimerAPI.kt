package com.inari.firefly.core.api

import com.inari.util.collection.DynArray
import com.inari.util.timeMillis
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object FFTimer : TimerAPI() {

    private var lastUpdateTime: Long = timeMillis()

    override var time: Long = 0
        private set
    override var timeElapsed: Long = 0
        private set

    private val schedulers: DynArray<UpdateScheduler> = DynArray.of( 20)

    override fun init() {
        lastUpdateTime = timeMillis()
    }
    override fun tick() {
        val currentTime = timeMillis()
        time += timeElapsed
        timeElapsed = currentTime - lastUpdateTime
        lastUpdateTime = currentTime
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
            } else /* if (updated) */ {
                needsUpdate = false
                //updated = false
            }
        }

        override fun needsUpdate(): Boolean {
//            if (needsUpdate)
//                updated = true
            return needsUpdate
        }

        fun reset() {
            lastUpdate = -1
            tick = 0
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
}

abstract class TimerAPI {

    abstract val time: Long
    abstract val timeElapsed: Long

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
        val it = schedulers.iterator()
        while (it.hasNext()) {
            val s = it.next()
            if (s.resolution == resolution) {
                return s
            }
        }
        val updateScheduler = FFTimer.UpdateScheduler(resolution)
        schedulers.add(updateScheduler)
        return updateScheduler
    }

    abstract fun init()
    abstract fun tick()

}