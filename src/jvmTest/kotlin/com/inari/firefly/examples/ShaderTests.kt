package com.inari.firefly.examples

import com.inari.firefly.BlendMode
import com.inari.firefly.DesktopRunner
import com.inari.firefly.FFContext
import com.inari.firefly.core.api.DesktopAppAdapter
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.effect.ShaderAsset
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.view.View
import com.inari.util.graphics.IColor

object ShaderTest1 {

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

    @JvmStatic fun main(args: Array<String>) {
        object : DesktopRunner("ShaderTest1", 600, 400) {
            override fun init() {

                val tex1Id = TextureAsset.buildAndActivate {
                    name = "Tex1"
                    resourceName = "firefly/alphaMaskCircle.png"
                }

                val shaderId = ShaderAsset.buildAndActivate {
                    name = "ShaderEffect1"
                    vertShaderProgram = DEFAULT_VERTEX_SHADER
                    fragShaderResource = "firefly/fragShaderTest1.glsl"
                    shaderInit =  { adapter ->
                        adapter.bindTexture("my_texture", FFContext[TextureAsset, tex1Id.instanceId].instanceId)
                    }
                }

                val viewId = View.buildAndActivate {
                    name = "View1"
                    bounds(100, 100, 100, 100)
                    clearColor = IColor.BLACK.mutable
                    blendMode = BlendMode.NONE
                    tintColor(1f,0f,0f,1f)          // this is the v_color
                    shader(shaderId)
                    zoom = 5f
                }

                Entity.buildAndActivate {
                    withComponent(ETransform) {
                        position(10, 10)
                        view(viewId)
                        scale(5f, 5f)
                    }
                    withComponent(EShape) {
                        color(1f, 0f, 0f, 1f)         // this color is on the u_texture shape
                        shapeType = ShapeType.RECTANGLE
                        fill = true
                        vertices = floatArrayOf( 10f, 10f, 50f, 50f)
                    }
                }
            }
        }
    }
}

object ShaderTest2 {
    @JvmStatic fun main(args: Array<String>) {
        object : DesktopRunner("ShaderTest2", 600, 400) {
            override fun init() {
                dispose()
                DesktopAppAdapter.exit()
            }
        }
    }
}