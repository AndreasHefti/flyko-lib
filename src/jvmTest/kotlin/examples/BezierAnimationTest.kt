package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.animation.*
import com.inari.util.geom.*

fun main() {
    DesktopApp("BezierAnimationTest", 400, 400) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(0)
                position(50f, 100f)
                pivot(0f, 0f)
            }
            withComponent(EShape) {
                type = ShapeType.RECTANGLE
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

                withAnimation(BezierCurveData) {
                    looping = true
                    inverseOnLoop = true
                    duration = 2500
                    easing = Easing.QUAD_IN_OUT
                    curve = bCurve
                    animatedXProperty = ETransform.PropertyAccessor.POSITION_X
                    animatedYProperty = ETransform.PropertyAccessor.POSITION_Y
                    animatedRotationProperty = ETransform.PropertyAccessor.ROTATION
                    animationController(BezierCurveAnimation)
                }
            }
        }

        Texture {
            name = "SpriteTextureAsset"
            resourceName = "firefly/atlas1616.png"
        }

        Sprite {
            name = "SpriteAsset"
            textureRef("SpriteTextureAsset")
            textureBounds(3 * 16, 16, 16, 16)
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(0)
                position(150f, 100f)
                pivot(8f, 8f)
            }
            withComponent(ESprite) {
                spriteRef("SpriteAsset")
            }
            withComponent(EAnimation) {
                val bCurve = CubicBezierCurve(
                    Vector2f(200f, 200f),
                    Vector2f(200f, 300f),
                    Vector2f(300f, 300f),
                    Vector2f(300f, 200f)
                )

                withAnimation(BezierCurveData) {
                    looping = true
                    inverseOnLoop = true
                    duration = 5000
                    easing = Easing.QUAD_IN_OUT
                    curve = bCurve
                    animatedXProperty = ETransform.PropertyAccessor.POSITION_X
                    animatedYProperty = ETransform.PropertyAccessor.POSITION_Y
                    animatedRotationProperty = ETransform.PropertyAccessor.ROTATION
                    animationController(BezierCurveAnimation)
                }
            }
        }
    }
}
