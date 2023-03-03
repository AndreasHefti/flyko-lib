package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.FFInfoSystem
import com.inari.firefly.graphics.FrameRateInfo
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.animation.*
import com.inari.util.geom.BezierSpline
import com.inari.util.geom.BezierSplineSegment
import com.inari.util.geom.Vector2f

fun main() {
    DesktopApp("BezierSplineAnimationTest", 400, 400) {

        FFInfoSystem
            .addInfo(FrameRateInfo)
            .activate()

        Texture {
            name = "SpriteTextureAsset"
            resourceName = "tiles/outline_16_16.png"
        }

        Sprite {
            name = "SpriteAsset"
            textureRef("SpriteTextureAsset")
            textureRegion(3 * 16, 16, 16, 16)
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(0)
                position(50f, 100f)
                pivot(8f, 8f)
            }
            withComponent(ESprite) {
                spriteRef("SpriteAsset")
            }
            withComponent(EAnimation) {
                val bezierSpline = BezierSpline()
                bezierSpline.add(
                    BezierSplineSegment(
                        2500,
                        Vector2f(50f, 200f),
                        Vector2f(50f, 50f),
                        Vector2f(200f, 50f),
                        Vector2f(200f, 200f)))
                bezierSpline.add(
                    BezierSplineSegment(
                    2500,
                    Vector2f(200f, 200f),
                    Vector2f(200f, 300f),
                    Vector2f(300f, 300f),
                    Vector2f(300f, 200f)))

                withAnimation(BezierSplineData) {
                    looping = true
                    inverseOnLoop = true
                    spline = bezierSpline
                    animatedXProperty = ETransform.PropertyAccessor.POSITION_X
                    animatedYProperty = ETransform.PropertyAccessor.POSITION_Y
                    animatedRotationProperty = ETransform.PropertyAccessor.ROTATION
                    animationController(BezierSplineAnimation)
                }
            }
        }
    }
}