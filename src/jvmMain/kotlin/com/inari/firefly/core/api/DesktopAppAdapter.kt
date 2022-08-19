package com.inari.firefly.core.api

import com.badlogic.gdx.Gdx
import com.inari.firefly.core.Engine

object DesktopAppAdapter : Engine(
    { GraphicsAPIImpl },
    { AudioAPIImpl },
    { InputAPIImpl },
    { FFTimer },
    { ResourceServiceAPIImpl }
) {

    fun exit() {
        Gdx.app.exit()
    }
}