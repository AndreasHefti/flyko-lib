package com.inari.firefly.examples

import com.inari.firefly.BlendMode
import com.inari.firefly.Color
import com.inari.firefly.DesktopRunner
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.TextureAsset
import com.inari.firefly.graphics.view.ShaderAsset
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.sprite.SpriteAsset
import com.inari.firefly.graphics.view.FrameBuffer
import com.inari.firefly.graphics.view.View

object NormalMapTest {

    @JvmStatic
    fun main(args: Array<String>) {
        object : DesktopRunner("NormalMapTest", 400, 400) {
            override fun init() {
                TextureAsset.buildAndActivate {
                    name = "TextureAsset"
                    resourceName = "firefly/normalMapTest.png"
                }

                val texId = SpriteAsset.buildAndActivate {
                    name = "Sprite"
                    texture("TextureAsset")
                    textureRegion(0, 0, 16, 16)
                }
                val normalSpriteId = SpriteAsset.buildAndActivate {
                    name = "Normal"
                    texture("TextureAsset")
                    textureRegion(16, 0, 16, 16)
                }

                val backBufferId = FrameBuffer.buildAndActivate {
                    name = "BackBuffer"
                    bounds(0, 0, 16, 16)
                    //view("View1")
                }

                val shaderId = ShaderAsset.buildAndActivate {
                    name = "NormalShader"
                    fragShaderResource = "firefly/normalFragShader.glsl"
                    shaderInit =  { adapter ->
                        //adapter.bindTexture("normal_texture", FFContext[SpriteAsset, normalSpriteId].instanceId)
                        adapter.bindBackBuffer("normal_texture", backBufferId.instanceId)
                    }
                }

                val viewId = View.buildAndActivate {
                    name = "View1"
                    bounds(100, 100, 100, 100)
                    clearColor = Color.BLACK.instance()
                    blendMode = BlendMode.NONE
                    tintColor(1f,1f,1f,1f)          // this is the v_color
                    shader(shaderId)
                }





                Entity.buildAndActivate {
                    withComponent(ETransform) {
                        position(0, 0)
                        view(viewId)
                    }
                    withComponent(ESprite) {
                        sprite("Sprite")
                    }

                }


            }
        }
    }
}