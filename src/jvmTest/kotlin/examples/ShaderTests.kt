package examples

import com.inari.firefly.*
import com.inari.firefly.core.Entity
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.sprite.Texture
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Shader
import com.inari.firefly.graphics.view.View
import com.inari.util.BLACK


private const val DEFAULT_VERTEX_SHADER =
    "attribute vec4 a_position;\n" +
    "attribute vec4 a_color;\n" +
    "attribute vec2 a_texCoord0;\n" +
    "uniform mat4 u_projTrans;\n" +
    "varying vec4 v_color;\n" +
    "varying vec2 v_texCoords;\n" +
    "void main() {\n" +
    "    v_color = a_color;\n" +
    "    v_texCoords = a_texCoord0;\n" +
    "    gl_Position = u_projTrans * a_position;\n" +
    "}\n"

private const val DEFAULT_FRAGMENT_SHADER =
    "#ifdef GL_ES\n" +
    "precision mediump float;\n" +
    "#endif\n" +
    "varying vec4 v_color;\n" +
    "varying vec2 v_texCoords;\n" +
    "uniform sampler2D u_texture;\n" +
    "uniform sampler2D my_texture;\n" +
    "void main()\n" +
    "{\n" +
//        "  gl_FragColor = vec4(1.0,0.0,0.0,1.0);\n" +
    "  gl_FragColor = vec4(0.0,1.0,0.0,1.0) + v_color * texture2D(my_texture, v_texCoords);\n" +
//        "  gl_FragColor =  texture2D(u_texture, v_texCoords) * texture2D(my_texture, v_texCoords) ;\n" +
//        "  gl_FragColor = mix(texture2D(u_texture, v_texCoords), texture2D(my_texture, v_texCoords), v_color) ;\n" +
    "}\n"

fun main(args: Array<String>) {
    DesktopApp("ShaderTest1", 600, 400) {

            val tex1Id = Texture {
                autoActivation = true
                name = "Tex1"
                resourceName = "firefly/alphaMaskCircle.png"
            }

            val shaderId = Shader {
                name = "ShaderEffect1"
                vertexShaderProgram = DEFAULT_VERTEX_SHADER
                fragmentShaderResourceName = "firefly/fragShaderTest1.glsl"
                shaderInit =  { adapter ->
                    adapter.bindTexture("my_texture", Texture[tex1Id].assetIndex)
                }
            }

            val viewId = View {
                autoActivation = true
                name = "View1"
                bounds(100, 100, 100, 100)
                clearColor(BLACK)
                blendMode = BlendMode.NONE
                tintColor(1f,0f,0f,1f)          // this is the v_color
                shader(shaderId)
                zoom = 5f
            }

            Entity {
                autoActivation = true
                withComponent(ETransform) {
                    position(10, 10)
                    viewRef(viewId)
                    scale(5f, 5f)
                }
                withComponent(EShape) {
                    color(1f, 0f, 0f, 1f)         // this color is on the u_texture shape
                    type = ShapeType.RECTANGLE
                    fill = true
                    vertices = floatArrayOf( 10f, 10f, 50f, 50f)
                }
            }
        }
}

