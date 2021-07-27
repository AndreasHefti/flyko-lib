package com.inari.firefly

import com.inari.util.event.EventDispatcher

object TestApp : FFApp(
    { EventDispatcher() },
    { GraphicsMock },
    { AudioMock },
    { InputMock },
    { TestTimer }
)