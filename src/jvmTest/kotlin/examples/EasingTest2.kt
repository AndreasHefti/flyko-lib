package examples

import com.inari.firefly.*
import com.inari.firefly.core.Engine.Companion.SYSTEM_FONT
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.text.EText
import com.inari.firefly.graphics.text.SimpleTextRenderer
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.animation.DefaultFloatEasingControl
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.firefly.util.geom.EasingTest

fun main(args: Array<String>) {
    DesktopApp( "CoverCodeTest", 800, 900) {

            FFInfoSystem
                .addInfo(FrameRateInfo)
                .activate()

        DefaultFloatEasingControl
//            FFContext.loadSystem(EntitySystem)
//            FFContext.loadSystem(AnimationSystem)

            for ((index, easingType) in EasingTest.EasingType.values().withIndex()) {
                createEasingAnimation(easingType, index)
            }
        }
    }

    private fun createEasingAnimation(type: EasingTest.EasingType, position: Int) {
        val ypos = position.toFloat() * (20f + 10f) + 50f

        Entity.buildActive {
            withComponent(ETransform) {
                view(0)
                position(10f, ypos)
            }
            withComponent(EText) {
                renderer = SimpleTextRenderer
                fontAssetRef(SYSTEM_FONT)
                text.append(type.name.replace("_", "-"))
                blend = BlendMode.NORMAL_ALPHA
                tint(1f, 1f, 1f, 1f)
            }
        }
        Entity.buildActive {
            withComponent(ETransform) {
                view(0)
            }
            withComponent(EShape) {
                this.type = ShapeType.RECTANGLE
                fill = true
                color1(1f, 0f, 0f, 1f)
                vertices = floatArrayOf(100f, ypos, 20f, 20f)
            }
            withComponent(EAnimation) {
                withAnimation(EasedFloatAnimation) {
                    looping = true
                    inverseOnLoop = true
                    easing = type.func
                    startValue = 100f
                    endValue = 400f
                    duration = 5000
                    animatedProperty = ETransform.PropertyAccessor.POSITION_X
                    animationController(DefaultFloatEasingControl)
                }
            }
        }
}
