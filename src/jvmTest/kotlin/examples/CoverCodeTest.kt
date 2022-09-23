package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.animation.DefaultFloatEasing
import com.inari.firefly.physics.animation.EAnimation
import com.inari.firefly.physics.animation.EasedFloatAnimation
import com.inari.util.geom.Easing

fun main(args: Array<String>) {
    DesktopApp( "CoverCodeTest", 800, 600) {

        // Create a TextureAsset and register it to the AssetSystem but not loading yet.
        Texture {
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            // Create and activate/load a SpriteAsset with reference to the TextureAsset.
            // This also implicitly loads the TextureAsset if it is not already loaded.
            withChild(Sprite) {
                name = "inariSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false
                vFlip = false
            }
        }
        // Create an Entity positioned on the base View on x=50/y=150, and the formerly
        // created sprite with a tint color. This also automatically loads the needed assets if not already done
        Entity {
            // automatically activate after creation
            autoActivation = true
            // add a transform component to the entity that defines the orientation of the Entity
            withComponent(ETransform) {
                viewRef(View.BASE_VIEW_KEY)
                position(50, 150)
                scale(4f, 4f)
            }
            // add a sprite component to the entity
            withComponent(ESprite) {
                spriteRef("inariSprite")
                tintColor(1f, 1f, 1f, .5f)
            }
            withComponent(EAnimation) {
                // with an active easing animation on the sprite alpha blending value...
                withAnimation(EasedFloatAnimation) {
                    looping = true
                    inverseOnLoop = true
                    easing = Easing.LINEAR
                    startValue = 0f
                    endValue = 1f
                    duration = 3000
                    animatedProperty = ESprite.PropertyAccessor.TINT_COLOR_ALPHA
                }
                // and with an active easing animation on the sprites position on the x axis...
                withAnimation(EasedFloatAnimation) {
                    looping = true
                    inverseOnLoop = true
                    easing = Easing.BACK_OUT
                    startValue = 50f
                    endValue = 400f
                    duration = 1000
                    animatedProperty = ETransform.PropertyAccessor.POSITION_X
                    animationController(DefaultFloatEasing)
                }
            }
        }
    }
}
