package com.inari.firefly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT_ASSET
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.graphics.sprite.SimpleSpriteRenderer
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.text.Font
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
        SimpleSpriteRenderer.init()

        InariIntro.show {
            ComponentSystem.clearSystems()
            initializer(this)
        }
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