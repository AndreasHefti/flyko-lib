package com.inari.firefly

import com.inari.firefly.core.Asset
import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputAPIImpl
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.animation.DefaultFloatEasing
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.util.geom.Easing
import org.lwjgl.glfw.GLFW

object InariIntro {

    private var textureAssetName = "INARI_LOGO_TEX"
    private var spriteAssetName = "INARI_LOGO_SPRITE"
    private var entityId = NO_COMPONENT_KEY
    private var callback: () -> Unit = {}
    private var disposing = false

    private const val pngWidth = 520
    private const val pngHeight = 139

    internal fun show(callback: () -> Unit) {
        this.callback = callback

        Texture.build {
            name = textureAssetName
            resourceName = "firefly/inari.png"
            withChild(Sprite) {
                name = spriteAssetName
                textureBounds( 0, 0, pngWidth, pngHeight )
            }
        }

        entityId = Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(View.BASE_VIEW_KEY)
                position(
                    Engine.graphics.screenWidth / 2 - pngWidth / 2,
                    Engine.graphics.screenHeight / 2 - pngHeight / 2
                )
            }
            withComponent(ESprite) {
                spriteRef(spriteAssetName)
                tintColor(1f, 1f, 1f, 0f)
            }
            withComponent(EAnimation) {
                withAnimation(EasedFloatAnimation) {
                    animatedProperty = ESprite.PropertyAccessor.TINT_COLOR_ALPHA
                    resetOnFinish = false
                    easing = Easing.LINEAR
                    startValue = 0f
                    endValue = 1f
                    duration = 1000
                    animationController(DefaultFloatEasing)
                }
            }
        }

        Engine.input.setKeyCallback { _, _, _ -> dispose() }
        Engine.input.setMouseButtonCallback { _, _ -> dispose() }
        val controllerInput = Engine.input.createDevice<InputAPIImpl.GLFWControllerInput>(
                "ControllerInput", InputAPIImpl.GLFWControllerInput)
        controllerInput.mapButtonInput(ButtonType.BUTTON_A, GLFW.GLFW_GAMEPAD_BUTTON_A)
        controllerInput.mapButtonInput(ButtonType.BUTTON_B, GLFW.GLFW_GAMEPAD_BUTTON_B)
        controllerInput.mapButtonInput(ButtonType.BUTTON_X, GLFW.GLFW_GAMEPAD_BUTTON_X)
        controllerInput.mapButtonInput(ButtonType.BUTTON_Y, GLFW.GLFW_GAMEPAD_BUTTON_Y)
        controllerInput.slot = 0
        Engine.input.setButtonCallback("ControllerInput") { _, _ -> dispose() }
    }

    private fun dispose() {

        if (!disposing) {
            println("dispose")
            disposing = true
            Entity.delete(entityId)
            Asset.delete(textureAssetName)
            entityId = NO_COMPONENT_KEY
            Engine.graphics.clearView(View[View.BASE_VIEW_KEY])
            Engine.input.resetInputCallbacks()
            callback()
        }
    }
}