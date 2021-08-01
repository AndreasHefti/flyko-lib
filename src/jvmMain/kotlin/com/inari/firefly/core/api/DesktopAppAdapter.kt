package com.inari.firefly.core.api

import com.badlogic.gdx.Gdx
import com.inari.firefly.FFApp
import com.inari.util.event.EventDispatcher

object DesktopAppAdapter : FFApp(
    { EventDispatcher() },
    { FFGraphics },
    { FFAudio },
    { FFInput },
    { FFTimer },
    { FFResourceService }
) {

    fun exit() {
        Gdx.app.exit()
    }
}