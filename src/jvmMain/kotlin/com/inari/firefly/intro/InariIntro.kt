package com.inari.firefly.intro

import com.inari.firefly.*
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.FFInput
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.util.Call
import com.inari.util.geom.Easing
import com.inari.util.geom.Rectangle
import org.lwjgl.glfw.GLFW

object InariIntro {

    private var textureAssetId = NO_COMP_ID
    private var spriteAssetId = NO_COMP_ID
    private var entityId = NO_COMP_ID
    private var animationId = NO_COMP_ID
    private var callback: Call = DO_NOTHING
    private var disposing = false

    internal fun show(callback: Call) {
        this.callback = callback

        val texture: TextureAsset = TextureAsset.buildActivateAndGet {
            resourceName = "firefly/inari.png"
        }
        textureAssetId = texture.componentId

        spriteAssetId = SpriteAsset.buildAndActivate {
            texture(textureAssetId)
            textureRegion = Rectangle( 0, 0, texture.width, texture.height )
        }

        entityId = Entity.buildAndActivate {
            withComponent(ETransform) {
                view(BASE_VIEW)
                position(
                    FFContext.screenWidth / 2 - texture.width / 2,
                    FFContext.screenHeight / 2 - texture.height / 2
                )
            }

            withComponent(ESprite) {
                sprite(spriteAssetId)
                tint(1f, 1f, 1f, 0f)
            }

            withComponent(EAnimation) {
                withAnimated<Float> {
                    animatedProperty = ESprite.Property.TINT_ALPHA
                    resetOnFinish = false
                    animationId = applyToNewActiveAnimation(EasedFloatAnimation) {
                        easing = Easing.Type.LINEAR
                        startValue = 0f
                        endValue = 1f
                        duration = 1000
                    }
                }
            }
        }

        FFContext.input.setKeyCallback { _, _, _ -> dispose() }
        FFContext.input.setMouseButtonCallback { _, _ -> dispose() }
        val controllerInput = FFContext.input.createDevice<FFInput.GLFWControllerInput>(
                "ControllerInput",
            FFInput.GLFWControllerInput, -1)
        controllerInput.mapButtonInput(ButtonType.BUTTON_A, GLFW.GLFW_GAMEPAD_BUTTON_A)
        controllerInput.mapButtonInput(ButtonType.BUTTON_B, GLFW.GLFW_GAMEPAD_BUTTON_B)
        controllerInput.mapButtonInput(ButtonType.BUTTON_X, GLFW.GLFW_GAMEPAD_BUTTON_X)
        controllerInput.mapButtonInput(ButtonType.BUTTON_Y, GLFW.GLFW_GAMEPAD_BUTTON_Y)
        controllerInput.slot = 0
        FFContext.input.setButtonCallback("ControllerInput") { _, _ -> dispose() }
    }

    private fun dispose() {
        if (!disposing) {
            disposing = true
            FFContext.delete(entityId)
            FFContext.delete(spriteAssetId)
            FFContext.delete(textureAssetId)
            FFContext.delete(animationId)
            FFContext.input.resetInputCallbacks()
            callback()
        }
    }
}