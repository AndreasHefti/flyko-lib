package com.inari.firefly.examples

import com.inari.firefly.*
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.view.ShaderAsset
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.graphics.view.FrameBuffer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewSystem
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector3f
import com.inari.util.geom.Vector4f

// With thanks to https://github.com/mattdesl/lwjgl-basics/wiki/ShaderLesson6
object NormalMapTest {

    const val DEFAULT_LIGHT_Z =  0.075f
    val LIGHT_COLOR = Vector4f(1f, 1f, 1f, 1f)
    val AMBIENT_COLOR = Vector4f(0.6f, 0.6f, 1f, 0.5f)
    val FALLOFF = Vector3f(.4f, 20f, 20f)

    @JvmStatic
    fun main(args: Array<String>) {
        object : DesktopRunner("NormalMapTest", 400, 400) {
            override fun init() {
                val texId = TextureAsset.buildAndActivate {
                    name = "TextureAsset"
                    resourceName = "firefly/normalMapTest.png"
                }

                SpriteAsset.buildAndActivate {
                    name = "Sprite"
                    texture("TextureAsset")
                    textureRegion(0, 0, 16, 16)
                }

                val pos = Vector3f( 0.0f, 0.0f, DEFAULT_LIGHT_Z)
                val shaderId = ShaderAsset.buildAndActivate {
                    name = "NormalShader"
                    fragShaderResource = "firefly/normalFragShader.glsl"
                    shaderInit =  { adapter ->
                        adapter.bindTexture("normal_texture", FFContext[TextureAsset, texId].instanceId)
                        adapter.setUniformVec2("Resolution", Vector2f(FFContext.graphics.screenWidth, FFContext.graphics.screenHeight))
                        adapter.setUniformColorVec4("LightColor", LIGHT_COLOR)
                        adapter.setUniformColorVec4("AmbientColor", AMBIENT_COLOR)
                        adapter.setUniformVec3("Falloff", FALLOFF)
                        adapter.setUniformVec3( "LightPos", pos)
                    }
                }

                val viewId = View.buildAndActivate {
                    name = "View1"
                    bounds(10, 10, 16, 16)
                    shader(shaderId)
                    blendMode = BlendMode.NONE
                    tintColor(1f, 1f, 1f, 1f)
                }

                val update = FFContext.timer.createUpdateScheduler(20f)
                FFContext.registerListener(FFApp.UpdateEvent) {
                    if (update.needsUpdate()) {
                        pos(
                            FFContext.input.xpos.toFloat() / FFContext.graphics.screenWidth,
                            1f - FFContext.input.ypos.toFloat() / FFContext.graphics.screenHeight)
                    }
                }

                ViewSystem.baseView.zoom = .2f
            }
        }
    }
}