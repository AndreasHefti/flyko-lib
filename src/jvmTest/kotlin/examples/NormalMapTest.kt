package examples

import com.inari.firefly.DesktopApp
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector3f
import com.inari.util.geom.Vector4f

// With thanks to https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson6

const val DEFAULT_LIGHT_Z =  0.075f
val LIGHT_COLOR = Vector4f(1f, .5f, .5f, 1f)
val AMBIENT_COLOR = Vector4f(0.6f, 0.6f, 1f, 0.5f)
val FALLOFF = Vector3f(.4f, 10f, 20f)

fun main() {
    DesktopApp("NormalMapTest", 400, 400) {

        val pos = Vector3f( 0.0f, 0.0f, DEFAULT_LIGHT_Z)

        View {
            autoActivation = true
            name = "View1"
            bounds(30, 30, 16, 16)
            blendMode = BlendMode.NONE
            tintColor(1f, 1f, 1f, 1f)

            // sprite and texture definition
            val texId = withChild(Texture) {
                name = "TextureAsset"
                resourceName = "firefly/normalMapTest.png"
                withChild(Sprite) {
                    name = "Sprite"
                    textureRegion(0, 0, 16, 16)
                }
            }

            // the shader definition
            withShader {
                name = "NormalShader"
                fragmentShaderResourceName = "firefly/normalFragShader.glsl"
                shaderUpdate =  { adapter ->
                    Texture.activate(texId)
                    adapter.bindTexture("normal_texture", Texture[texId].assetIndex)
                    adapter.setUniformVec2("Resolution", Vector2f(Engine.graphics.screenWidth, Engine.graphics.screenHeight))
                    adapter.setUniformColorVec4("LightColor", LIGHT_COLOR)
                    adapter.setUniformColorVec4("AmbientColor", AMBIENT_COLOR)
                    adapter.setUniformVec3("Falloff", FALLOFF)
                    adapter.setUniformVec3( "LightPos", pos)
                }
            }
        }

        // update adapter
        val update = Engine.timer.createUpdateScheduler(20f)
        Engine.registerListener(Engine.UPDATE_EVENT_TYPE) {
            if (update.needsUpdate()) {
                pos(
                    Engine.input.xpos.toFloat() / Engine.graphics.screenWidth,
                    1f - Engine.input.ypos.toFloat() / Engine.graphics.screenHeight)
            }
        }

        View[View.BASE_VIEW_KEY].zoom = .2f
    }
}