package com.inari.firefly

import com.inari.firefly.core.api.TimerAPI

object TestTimer : TimerAPI() {

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
