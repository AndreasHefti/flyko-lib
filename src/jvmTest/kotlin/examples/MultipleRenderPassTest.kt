package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.GraphicsAPIImpl
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.View

fun main() {
    DesktopApp("MultipleRenderPassTest", 800, 600) {

        Texture.build {
            name = "logoTexture"
            resourceName = "firefly/logo.png"
            withChild(Sprite) {
                name = "inariSprite"
                textureBounds(0, 0, 32, 32)
                hFlip = false
                vFlip = false
            }
        }

        // create a back-buffer view where we can render to and that will be used as source
        // for another view (main view) that is finally rendered to the world canvas.
        // With this setup we have two render passes after sprite rendering. The rendered sprites are drawn into the
        // back-buffer view within the first rendering phase. Then the back-buffer view is rendered to the main view
        // using shaderB. Then the main view is drawn to the canvas view using shaderA.

        View {
            autoActivation = true
            name = "mainView"
            bounds(0, 0, 100, 100)
            zoom = .5f
            withShader {
                name = "shaderA"
                vertexShaderResourceName = "firefly/vertBloom.glsl"
                fragmentShaderResourceName = "firefly/fragBloom.glsl"
                //vertexShaderProgram = GraphicsAPIImpl.DEFAULT_VERTEX_SHADER
                //fragmentShaderProgram = GraphicsAPIImpl.DEFAULT_FRAGMENT_SHADER
            }
            excludeFromEntityRendering = true
            asRenderTargetOf {
                name = "back-buffer"
                bounds(0, 0, 100, 100)
                withShader {
                    name = "shaderB"
                    vertexShaderResourceName = "firefly/vertBloom.glsl"
                    fragmentShaderResourceName = "firefly/fragBloom.glsl"
                    //vertexShaderProgram = GraphicsAPIImpl.DEFAULT_VERTEX_SHADER
                    //fragmentShaderProgram = GraphicsAPIImpl.DEFAULT_FRAGMENT_SHADER
                }
            }
        }

        Entity {
            autoActivation = true
            withComponent(ETransform) {
                position(0, 0)
                viewRef("back-buffer")
                //scale(3f, 3f)
            }
            withComponent(ESprite) {
                spriteRef("inariSprite")
            }
        }
    }
}