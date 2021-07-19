package com.inari.firefly.examples

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.DesktopApp
import com.inari.firefly.core.api.DesktopAppAdapter


class IntroTest : DesktopApp() {

    override val title: String = "IntroTest"

    override fun init() {
        dispose()
        DesktopAppAdapter.exit()
    }

}

fun main(args: Array<String>) {
    try {
        val config = Lwjgl3ApplicationConfiguration()
        config.setResizable(true)
        config.setWindowedMode(704, 480)
        Lwjgl3Application(IntroTest(), config)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}