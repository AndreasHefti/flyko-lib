package com.inari.firefly.examples

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.inari.firefly.BASE_VIEW
import com.inari.firefly.DesktopApp
import com.inari.firefly.DesktopRunner
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.util.geom.Easing

fun main(args: Array<String>) {
    object : DesktopRunner( "CoverCodeTest", 800, 600) {

        // This is called from the API at startup to do all the initialization setup
        override fun init() {

            FFInfoSystem
                .addInfo(FrameRateInfo)
                .activate()

            // Create a TextureAsset and register it to the AssetSystem but not loading yet.
            //
            // WIth this method you are able to define all your assets in one place without
            // loading the assets into memory yet. When you need them you can simply load
            // them by calling FFContext.activate(TextureAsset, "logoTexture") and dispose them
            // with FFContext.dispose(TextureAsset, "logoTexture"). The asset definition is still
            // available and can be deleted with FFContext.delete(TextureAsset, "logoTexture")
            val texAssetId = TextureAsset.build {
                name = "logoTexture"
                resourceName = "firefly/logo.png"
            }

            // Create and activate/load a SpriteAsset with reference to the TextureAsset.
            // This also implicitly loads the TextureAsset if it is not already loaded.
            val spriteId = SpriteAsset.buildAndActivate {
                // It would also be possible to use the name of the texture asset here
                // instead of the identifier. But of corse, identifier (index) gives faster access
                texture(texAssetId)
                textureRegion(0, 0, 32, 32)
                horizontalFlip = false
                verticalFlip = false
            }

            // Create an Entity positioned on the base View on x=50/y=150, and the formerly
            // created sprite with a tint color.
            // Add also two animation, one for the alpha of the tint color and one for the
            // position on the x axis and activate everything immediately.
            val entityId = Entity.buildAndActivate {

                // add a transform component to the entity that defines the orientation of the Entity
                withComponent(ETransform) {
                    view(BASE_VIEW)
                    position(50, 150)
                    scale(4f, 4f)
                }

                // add a sprite component to the entity
                withComponent(ESprite) {
                    sprite(spriteId)
                    tint(1f, 1f, 1f, .5f)
                }

                // add an animation component to the entity that defines an animation based on
                // the alpha value of the color property of the sprite.
                //
                // Animations normally can work for itself and lifes in the AnimationSystem. But if
                // a property of an Entity-Component like ESprite defines a property value adapter,
                // an animation can be bound to this property directly to affecting the value of the property.
                withComponent(EAnimation) {

                    // with an active easing animation on the sprite alpha blending value...
                    withAnimated<Float> {
                        looping = true
                        inverseOnLoop = true
                        // that is connected to the alpha value of the sprite of the entity
                        animatedProperty = ESprite.Property.TINT_ALPHA
                        withActiveAnimation(EasedFloatAnimation) {
                            easing = Easing.LINEAR
                            startValue = 0f
                            endValue = 1f
                            duration = 3000
                        }
                    }

                    // and with an active easing animation on the sprites position on the x axis...
                    withAnimated<Float> {
                        looping = true
                        inverseOnLoop = true
                        // that is connected to the position value on the x axis of the entities transform data
                        animatedProperty = ETransform.Property.POSITION_X
                        withActiveAnimation(EasedFloatAnimation) {
                            easing = Easing.BACK_OUT
                            startValue = 50f
                            endValue = 400f
                            duration = 1000
                        }
                    }
                }
            }
        }
    }
}
