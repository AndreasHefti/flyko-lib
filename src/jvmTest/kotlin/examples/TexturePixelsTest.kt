package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Vector4i

fun main() {
    DesktopApp("TexturePixelGrabTest", 400, 200) {

        val texId = Texture {
            autoActivation = true
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            withSprite {
                name = "inariSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false
                vFlip = false
            }
        }

        val texBytes = Engine.graphics.getTexturePixels(Texture[texId].assetIndex)
        texBytes.forEach { b ->
            print(b)
            print(" ")
        }
        println(texBytes.size)

        val clear = ByteArray(1024) { -1 }

        Engine.graphics.setTexturePixels(Texture[texId].assetIndex, Vector4i(0, 0, 15, 15), clear)

        Entity {
            // automatically activate after creation
            autoActivation = true
            // add a transform component to the entity that defines the orientation of the Entity
            withComponent(ETransform) {
                viewRef(View.BASE_VIEW_KEY)
            }
            // add a sprite component to the entity
            withComponent(ESprite) {
                spriteRef("inariSprite")
            }
        }

    }
}