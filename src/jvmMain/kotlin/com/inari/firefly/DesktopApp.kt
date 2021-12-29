package com.inari.firefly

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.control.ControllerSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.control.trigger.TriggerSystem
import com.inari.firefly.control.trigger.UpdateEventTrigger
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.rendering.RenderingSystem
import com.inari.firefly.graphics.text.FontAsset
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.firefly.intro.InariIntro
import com.inari.firefly.physics.animation.AnimationSystem
import kotlin.math.roundToInt


abstract class DesktopApp : ApplicationAdapter() {

    abstract val title: String

    override fun create() {
        Gdx.graphics.setTitle(title)

        // load the app
        DesktopAppAdapter
        // load some initial systems
        AssetSystem
        ViewSystem
        TaskSystem
        RenderingSystem
        EntitySystem
        TriggerSystem
        ControllerSystem
        AnimationSystem
        // ...

        InariIntro.show {
            loadSystemFont()
            init()
        }
    }

    protected abstract fun init()

    override fun render() {
        DesktopAppAdapter.update()
        DesktopAppAdapter.render()
    }

    private fun loadSystemFont() {
        TextureAsset.build {
            name = SYSTEM_FONT_ASSET
            resourceName = "firefly/fireflyMicroFont.png"
        }

        FontAsset.buildAndActivate {
            name = SYSTEM_FONT
            texture(SYSTEM_FONT_ASSET)
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

    fun fitBaseViewportToScreen(width: Int, height: Int, baseWidth: Int, baseHeight: Int, centerCamera: Boolean) {
        if (width <= 0 || height <= 0)
            return

        val bounds = ViewSystem.baseView.bounds
        val worldPosition = ViewSystem.baseView.worldPosition
        val targetRatio = height.toFloat() / width
        val sourceRatio = baseHeight.toFloat() / baseWidth
        val fitToWidth = targetRatio > sourceRatio
        val zoom = ViewSystem.baseView.zoom

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
        TriggerSystem
        UpdateEventTrigger.build {
            call = {
                dispose()
                DesktopAppAdapter.exit()
            }
            condition = {
                Gdx.input.isKeyPressed( key )
            }
        }
    }
}