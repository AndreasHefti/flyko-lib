package com.inari.firefly.examples

import com.inari.firefly.DesktopRunner
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.util.geom.Easing

object RotationTest {

    @JvmStatic fun main(args: Array<String>) {
        object : DesktopRunner("RotationTest", 400, 400) {
            override fun init() {
                Entity.buildAndActivate {
                    withComponent(ETransform) {
                        view(0)
                        position(100f, 100f)
                        pivot(0f, 0f)
                    }
                    withComponent(EShape) {
                        this.shapeType = ShapeType.RECTANGLE
                        fill = true
                        color(1f, 0f, 0f, 1f)
                        vertices = floatArrayOf(-10f, -10f, 20f, 20f)
                    }
                    withComponent(EAnimation) {
                        withAnimated<Float> {
                            looping = true
                            inverseOnLoop = true
                            animatedProperty = ETransform.Property.ROTATION
                            withActiveAnimation(EasedFloatAnimation) {
                                startValue = 0f
                                endValue = -90f
                                duration = 5000
                                easing = Easing.LINEAR
                            }
                        }
                        withAnimated<Float> {
                            looping = true
                            inverseOnLoop = true
                            animatedProperty = ETransform.Property.SCALE_X
                            withActiveAnimation(EasedFloatAnimation) {
                                startValue = 1f
                                endValue = 2f
                                duration = 5000
                                easing = Easing.LINEAR
                            }
                        }
                    }
                }

                TextureAsset.buildAndActivate {
                    name = "SpriteTextureAsset"
                    resourceName = "tiles/outline_16_16.png"
                }

                SpriteAsset.buildAndActivate {
                    name = "SpriteAsset"
                    texture("SpriteTextureAsset")
                    textureRegion(3 * 16, 16, 16, 16)
                }

                Entity.buildAndActivate {
                    withComponent(ETransform) {
                        view(0)
                        position(150f, 100f)
                        pivot(8f, 8f)
                    }
                    withComponent(ESprite) {
                        sprite("SpriteAsset")
                    }
                    withComponent(EAnimation) {
                        withAnimated<Float> {
                            looping = true
                            inverseOnLoop = true
                            animatedProperty = ETransform.Property.ROTATION
                            withActiveAnimation(EasedFloatAnimation) {
                                startValue = 0f
                                endValue = -90f
                                duration = 5000
                                easing = Easing.LINEAR
                            }
                        }

                    }
                }
            }
        }
    }
}