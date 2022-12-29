package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.util.BLACK

fun main() {
    DesktopApp("ViewTest", 800, 600) {
        Texture.build {
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

        val viewKey = View {
            autoActivation = true
            name = "view1"
            renderPassTo(View.BASE_VIEW_KEY)
            bounds(0, 0, 100, 100)
            clearColor(BLACK)
            blendMode = BlendMode.NORMAL_ALPHA
            zoom = .5f
            withLayer {
                name = "layer1"
            }
        }


        Entity {
            autoActivation = true
            withComponent(ETransform) {
                viewRef(viewKey)
                layerRef("layer1")
                position(10, 10)
            }
            withComponent(ESprite) {
                spriteRef("inariSprite")
                tintColor(1f, 1f, 1f, 1f)
            }
        }
    }
}