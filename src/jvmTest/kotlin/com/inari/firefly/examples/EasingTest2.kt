package com.inari.firefly.examples

import com.inari.firefly.*
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.rendering.RenderingSystem
import com.inari.firefly.graphics.rendering.SimpleTextRenderer
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.info.FFInfoSystem
import com.inari.firefly.info.FrameRateInfo
import com.inari.firefly.physics.animation.AnimationSystem
import com.inari.firefly.physics.animation.easing.EasedProperty
import com.inari.firefly.physics.animation.entity.EAnimation
import com.inari.util.geom.Easing

fun main(args: Array<String>) {
    object : DesktopRunner("EasingTest2", 800, 900) {
        override fun init() {
            FFInfoSystem
                .addInfo(FrameRateInfo)
                .activate()
            RenderingSystem
            FFContext.loadSystem(EntitySystem)
            FFContext.loadSystem(AnimationSystem)

            for ((index, easingType) in Easing.Type.values().withIndex()) {
                createEasingAnimation(easingType, index)
            }
        }

        private fun createEasingAnimation(type: Easing.Type, position: Int) {
            val ypos = position.toFloat() * (20f + 10f) + 50f
            Entity.buildAndActivate {
                withComponent(ETransform) {
                    view(0)
                    position(10f, ypos)
                }
                withComponent(EText) {
                    renderer(SimpleTextRenderer)
                    fontAsset(SYSTEM_FONT)
                    text.append(type.name.replace("_", "-"))
                    blend = BlendMode.NORMAL_ALPHA
                    tint(1f, 1f, 1f, 1f)
                }
            }
            Entity.buildAndActivate {
                withComponent(ETransform) {
                    view(0)
                }
                withComponent(EShape) {
                    this.shapeType = ShapeType.RECTANGLE
                    fill = true
                    color(1f, 0f, 0f, 1f)
                    vertices = floatArrayOf(100f, ypos, 20f, 20f)
                }
                withComponent(EAnimation) {
                    withActiveAnimation(EasedProperty) {
                        looping = true
                        inverseOnLoop = true
                        startValue = 100f
                        endValue = 400f
                        duration = 5000
                        easing = type
                        propertyRef = ETransform.Property.POSITION_X
                    }
                }
            }
        }
    }
}
