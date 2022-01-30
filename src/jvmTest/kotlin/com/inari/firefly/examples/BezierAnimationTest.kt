package com.inari.firefly.examples

import com.inari.firefly.DesktopRunner
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.animation.*
import com.inari.util.geom.*

object BezierAnimationTest {

    @JvmStatic fun main(args: Array<String>) {
        object : DesktopRunner("BezierAnimationTest", 400, 400) {
            override fun init() {
                FFInfoSystem
                    .addInfo(FrameRateInfo)
                    .activate()

                Entity.buildAndActivate {
                    withComponent(ETransform) {
                        view(0)
                        position(50f, 100f)
                        pivot(0f, 0f)
                    }
                    withComponent(EShape) {
                        this.shapeType = ShapeType.RECTANGLE
                        fill = true
                        color(1f, 0f, 0f, 1f)
                        vertices = floatArrayOf(-10f, -10f, 20f, 20f)
                    }
                    withComponent(EAnimation) {
                        val bCurve = CubicBezierCurve(
                            Vector2f(50f, 200f),
                            Vector2f(50f, 50f),
                            Vector2f(200f, 50f),
                            Vector2f(200f, 200f),
                        )

                        withAnimated<ETransform> {
                            looping = true
                            inverseOnLoop = true
                            animatedProperty = ETransform.Property.TRANSFORM
                            withActiveAnimation(BezierAnimation) {
                                curve = bCurve
                                duration = 5000
                                easing = Easing.QUAD_IN_OUT
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
                        val bCurve = CubicBezierCurve(
                            Vector2f(200f, 200f),
                            Vector2f(200f, 300f),
                            Vector2f(300f, 300f),
                            Vector2f(300f, 200f)
                        )

                        withAnimated<ETransform> {
                            looping = true
                            inverseOnLoop = true
                            animatedProperty = ETransform.Property.TRANSFORM
                            withActiveAnimation(BezierAnimation) {
                                curve = bCurve
                                duration = 5000
                                easing = Easing.QUAD_IN_OUT
                            }
                        }
                    }
                }
            }
        }
    }

    object BezierSplineAnimationTest {

        @JvmStatic
        fun main(args: Array<String>) {
            object : DesktopRunner("BezierAnimationTest", 400, 400) {
                override fun init() {
                    FFInfoSystem
                        .addInfo(FrameRateInfo)
                        .activate()

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
                            position(50f, 100f)
                            pivot(8f, 8f)
                        }
                        withComponent(ESprite) {
                            sprite("SpriteAsset")
                        }
                        withComponent(EAnimation) {
                            val bezierSpline = BezierSpline()
                            bezierSpline.add(
                                BezierSplineSegment(
                                    5000,
                                    Vector2f(50f, 200f),
                                    Vector2f(50f, 50f),
                                    Vector2f(200f, 50f),
                                    Vector2f(200f, 200f)
                                )
                            )
                            bezierSpline.add(BezierSplineSegment(
                                5000,
                                Vector2f(200f, 200f),
                                Vector2f(200f, 300f),
                                Vector2f(300f, 300f),
                                Vector2f(300f, 200f)
                            ))


                            withAnimated<ETransform> {
                                looping = true
                                inverseOnLoop = true
                                animatedProperty = ETransform.Property.TRANSFORM
                                withActiveAnimation(BezierSplineAnimation) {
                                    spline = bezierSpline
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
