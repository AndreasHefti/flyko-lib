package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View

fun main() {
    DesktopApp("RetroShaderTest", 800, 600) {

        Texture.build {
            name = "Into"
            resourceName = "firefly/inari.png"
            withSprite {
                name = "IntroSprite"
                textureBounds( 0, 0, 520, 139)
            }
        }

        View {
            autoActivation = true
            name = "View1"
            bounds(0, 0, 800, 600)
            withShader {
                name = "ShaderEffect1"
                vertexShaderResourceName = "firefly/vertRetro.glsl"
                fragmentShaderResourceName = "firefly/fragRetro.glsl"
                shaderUpdate = { adapter ->
                    adapter.setUniformVec2("u_res", View.baseViewPortProjectionSize)
                }
            }
            clearColor(.1f, .1f, .1f, 1f)
            zoom = 1f
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(100, 100)
                viewRef("View1")
            }
            withComponent(ESprite) {
                spriteRef("IntroSprite")
            }
        }
    }
}