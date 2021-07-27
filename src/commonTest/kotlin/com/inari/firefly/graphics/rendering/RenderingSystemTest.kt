package com.inari.firefly.graphics.rendering

import com.inari.firefly.GraphicsMock
import com.inari.firefly.TestApp
import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.asset.TestAsset
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.sprite.ESprite
import com.inari.firefly.graphics.view.ViewSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class RenderingSystemTest {

    @Test
    fun testSystemInit() {
        TestApp
        RenderingSystem.clearSystem()

        assertSame(RenderingSystem[SimpleSpriteRenderer], SimpleSpriteRenderer.instance)
    }

    @Test
    fun testRenderSprite() {
        TestApp
        AssetSystem.clearSystem()
        ViewSystem.clearSystem()
        RenderingSystem.clearSystem()
        EntitySystem.clearSystem()
        GraphicsMock.clearLogs()

        val assetId = TestAsset.buildAndActivate {
            name = "Test"
        }

        Entity.buildAndActivate {
            withComponent(ETransform) {
                view(0)
                pivot.x = 1f
                pivot.y = 2f
            }
            withComponent(ESprite) {
                sprite( assetId)
            }
        }

        assertEquals("[]", GraphicsMock.log())

        TestApp.render()

        assertEquals(
            "[startRendering::ViewData(bounds=[x=0,y=0,width=100,height=100], worldPosition=[x=0.0,y=0.0], clearColor=[r=0.0,g=0.0,b=0.0,a=1.0], tintColor=[r=1.0,g=1.0,b=1.0,a=1.0], blendMode=NONE, shaderId=-1, zoom=1.0, fboScale=1.0, index=0, isBase=true), renderSprite::Sprite(SpriteRenderable(spriteId=1, tintColor=[r=1.0,g=1.0,b=1.0,a=1.0], blendMode=NONE, shaderId=-1)), endRendering::ViewData(bounds=[x=0,y=0,width=100,height=100], worldPosition=[x=0.0,y=0.0], clearColor=[r=0.0,g=0.0,b=0.0,a=1.0], tintColor=[r=1.0,g=1.0,b=1.0,a=1.0], blendMode=NONE, shaderId=-1, zoom=1.0, fboScale=1.0, index=0, isBase=true), flush]",
            GraphicsMock.log())
    }
}