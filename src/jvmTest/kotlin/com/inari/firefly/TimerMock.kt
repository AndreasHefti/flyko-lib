package com.inari.firefly

import com.inari.firefly.core.api.TimerAPI

object TestTimer : TimerAPI() {

    override var time: Long = 0
    override var timeElapsed: Long = 0

    override fun init() {
        time = 0
    }
    override fun tick() {
        time++
        timeElapsed++
        Unit
    }

    internal fun reset() {
        time = 0
        timeElapsed = 0
    }

}
