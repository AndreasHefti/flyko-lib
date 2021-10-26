package com.inari.firefly

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

abstract class AppRunner (
    appTitle: String,
    width: Int,
    height: Int,
    resizable: Boolean = true,
    vsync: Boolean = true
) {

    init {
        try {
            val config = Lwjgl3ApplicationConfiguration()
            config.setResizable(resizable)
            config.setWindowedMode(width, height)
            config.useVsync(vsync)
            val desktopApp =  object : DesktopApp() {
                override val title = appTitle
                override fun init() = this@AppRunner.init(this)
            }

            Lwjgl3Application(desktopApp, config)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    abstract fun init(desktopApp: DesktopApp)
}