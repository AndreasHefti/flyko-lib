package com.inari.firefly

import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.ResourceServiceAPIImpl

object TestApp : Engine(
    { GraphicsMock },
    { AudioMock },
    { InputMock },
    { TestTimer },
    { ResourceServiceAPIImpl }
) {

    fun resetTimer() = (timer as TestTimer).reset()

}