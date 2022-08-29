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
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import kotlin.math.roundToInt


class DesktopApp(
    val title: String,
    private val defaultWidth: Int = 800,
    private val defaultHeight: Int = 600,
    resizable: Boolean = true,
    vsync: Boolean = true,
    icon: String? = null,
    private val fitBaseViewPortToScreen: Boolean = true,
    private val centerCamera: Boolean = true,
    val initializer: (DesktopApp) -> Unit
) : ApplicationAdapter() {

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
        MultiPositionSpriteRenderer.init()
        SpriteGroupRenderer.init()
        SimpleSpriteRenderer.init()
        SimpleTextRenderer.init()
        SimpleShapeRenderer.init()

        InariIntro.show {
            ComponentSystem.clearSystems()
            loadSystemFont()
            ComponentSystem.dumpInfo()
            initializer(this)
        }
    }

    private fun loadSystemFont() {
        Texture.build {
            name = Engine.SYSTEM_FONT_ASSET
            resourceName = "firefly/fireflyMicroFont.png"

            withChild(Font) {
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
        if (fitBaseViewPortToScreen)
            fitBaseViewportToScreen(width, height, defaultWidth, defaultHeight, centerCamera)
    }

    fun fitBaseViewportToScreen(width: Int, height: Int, baseWidth: Int, baseHeight: Int, centerCamera: Boolean) {
        if (width <= 0 || height <= 0)
            return
        val baseView = View[View.BASE_VIEW_KEY]
        val bounds = baseView.bounds
        val worldPosition = baseView.worldPosition
        val targetRatio = height.toFloat() / width
        val sourceRatio = baseHeight.toFloat() / baseWidth
        val fitToWidth = targetRatio > sourceRatio
        val zoom = baseView.zoom

        if (fitToWidth) {
            bounds.width = baseWidth
            bounds.height = (baseHeight / sourceRatio * targetRatio).roundToInt()
        } else {
            bounds.width = (baseWidth / targetRatio * sourceRatio).roundToInt()
            bounds.height = baseHeight
        }

        if (centerCamera) {
            worldPosition.x = -(bounds.width - baseWidth).toFloat() / 2 * zoom
            worldPosition.y = -(bounds.height - baseHeight).toFloat() / 2 * zoom
        }
    }

    fun addExitKeyTrigger(key: Int) {
        Trigger
        UpdateEventTrigger.build {
            call = {
                dispose()
                DesktopAppAdapter.exit()
                true
            }
            condition = {
                Gdx.input.isKeyPressed( key )
            }
        }
    }
}