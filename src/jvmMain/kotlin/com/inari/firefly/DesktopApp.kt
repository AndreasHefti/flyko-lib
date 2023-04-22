package com.inari.firefly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.core.*
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.graphics.shape.SimpleShapeRenderer
import com.inari.firefly.graphics.sprite.MultiPositionSpriteRenderer
import com.inari.firefly.graphics.sprite.SimpleSpriteRenderer
import com.inari.firefly.graphics.sprite.SpriteGroupRenderer
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.text.Font
import com.inari.firefly.graphics.text.SimpleTextRenderer
import com.inari.firefly.graphics.tile.SimpleTileGridRenderer
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.util.VOID_CONSUMER
import org.lwjgl.glfw.GLFW


class DesktopApp(
    private val title: String,
    private val defaultWidth: Int = 800,
    private val defaultHeight: Int = 600,
    resizable: Boolean = true,
    vsync: Boolean = true,
    icon: String? = null,
    private val debug: Boolean = false,
    val initializer: (DesktopApp) -> Unit
) : ApplicationAdapter() {

    var onDispose: (DesktopApp) -> Unit = VOID_CONSUMER

    init {
        try {
            val config = Lwjgl3ApplicationConfiguration()
            config.setResizable(resizable)
            config.setWindowedMode(defaultWidth, defaultHeight)
            config.useVsync(vsync)
            if (icon != null)
                config.setWindowIcon(icon)

            Lwjgl3Application(this, config)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun create() {
        Gdx.graphics.setTitle(title)

        // load the app
        DesktopAppAdapter

        // init core systems
        Control
        Trigger
        Asset
        Font
        Entity
        View
        Layer

        // init base renderer pipeline
        SimpleTileGridRenderer.init()
        MultiPositionSpriteRenderer.init()
        SpriteGroupRenderer.init()
        SimpleSpriteRenderer.init()
        SimpleTextRenderer.init()
        SimpleShapeRenderer.init()
        // init timer
        Engine.timer.init()

        InariIntro.show {
            ComponentSystem.clearSystems()
            loadSystemFont()
            initializer(this)

            if (debug) {
                ComponentSystem.dumpInfo()
                val runtime = Runtime.getRuntime()
                val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
                val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
                val availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB
                println("*************************************************************************")
                println("Java Version: : ${java.lang.System.getProperty("java.version")}")
                println("usedMemInMB: : $usedMemInMB")
                println("maxHeapSizeInMB: : $maxHeapSizeInMB")
                println("availHeapSizeInMB: : $availHeapSizeInMB")
                println("*************************************************************************")
            }
        }
    }

    private fun loadSystemFont() {
        Texture.build {
            survivesSystemClear = true
            name = Engine.SYSTEM_FONT_ASSET
            resourceName = "firefly/fireflyMicroFont.png"

            withFont {
                survivesSystemClear = true
                name = Engine.SYSTEM_FONT
                charWidth = 8
                charHeight = 16
                charSpace = 0
                lineSpace = 0
                defaultChar = 'a'
                charMap = arrayOf(
                    charArrayOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',' '),
                    charArrayOf('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',' '),
                    charArrayOf('1','2','3','4','5','6','7','8','9','0','!','@','£','$','%','?','&','*','(',')','-','+','=','"','.',',',':')
                )
            }
        }

        Font.activate(Engine.SYSTEM_FONT)
    }

    override fun render() = DesktopAppAdapter.update()

    override fun resize(width: Int, height: Int) {
        if (View.fitBaseViewPortToScreen)
            View.notifyScreenSizeChange(width, height, defaultWidth, defaultHeight)
    }

    override fun dispose() = onDispose(this)

    fun addExitKeyTrigger(key: Int) {
        Trigger
        UpdateEventTrigger.build {
            autoActivation = true
            call = {
                DesktopAppAdapter.exit()
                true
            }
            condition = {
                Gdx.input.isKeyPressed( key )
            }
        }
    }
}