package com.inari.firefly

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

abstract class DesktopRunner (
    appTitle: String,
    private val defaultWidth: Int,
    private val defaultHeight: Int,
    resizable: Boolean = true,
    vsync: Boolean = true,
    icon: String? = null,
    private val fitBaseViewPortToScreen: Boolean = true,
    private val centerCamera: Boolean = true
) {

    private lateinit var desktopApp: DesktopApp

    init {
        try {
            val config = Lwjgl3ApplicationConfiguration()
            config.setResizable(resizable)
            config.setWindowedMode(defaultWidth, defaultHeight)
            config.useVsync(vsync)
            if (icon != null)
                config.setWindowIcon(icon)
            desktopApp = object : DesktopApp() {
                override val title = appTitle
                override fun init() = this@DesktopRunner.init()
                override fun resize(width: Int, height: Int) = this@DesktopRunner.resize(width, height)
            }

            Lwjgl3Application(desktopApp, config)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    protected fun dispose() = desktopApp.dispose()

    abstract fun init()

    protected fun resize(width: Int, height: Int) {
        if (fitBaseViewPortToScreen)
            desktopApp.fitBaseViewportToScreen(width, height, defaultWidth, defaultHeight, centerCamera)
    }

}