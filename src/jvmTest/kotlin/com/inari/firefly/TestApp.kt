package com.inari.firefly

import com.inari.firefly.core.api.FFResourceService
import com.inari.util.event.EventDispatcher

object TestApp : FFApp(
    { EventDispatcher() },
    { GraphicsMock },
    { AudioMock },
    { InputMock },
    { TestTimer },
    { FFResourceService }
) {

    fun resetTimer() = (timer as TestTimer).reset()

}