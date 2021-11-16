package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4i

actual object FFGraphics : GraphicsAPI {

    actual override val screenWidth: Int
        get() = TODO("Not yet implemented")

    actual override val screenHeight: Int
        get() = TODO("Not yet implemented")

    actual override fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        TODO("Not yet implemented")
    }

    actual override fun disposeTexture(textureId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun createSprite(data: SpriteData): Int {
        TODO("Not yet implemented")
    }

    actual override fun disposeSprite(spriteId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun createShader(data: ShaderData): Int {
        TODO("Not yet implemented")
    }

    actual override fun disposeShader(shaderId: Int) {
    }

    actual override fun createBackBuffer(data: BackBufferData): Int {
        TODO("Not yet implemented")
    }

    actual override fun disposeBackBuffer(backBufferId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun startRendering(view: ViewData, clear: Boolean) {
        TODO("Not yet implemented")
    }

    actual override fun startBackBufferRendering(backBufferId: Int, posX: Float, posY: Float, clear: Boolean) {
        TODO("Not yet implemented")
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float) {
        TODO("Not yet implemented")
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData) {
        TODO("Not yet implemented")
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float) {
        TODO("Not yet implemented")
    }

    actual override fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
        TODO("Not yet implemented")
    }

    actual override fun renderShape(data: ShapeData, transform: TransformData) {
        TODO("Not yet implemented")
    }

    actual override fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float) {
        TODO("Not yet implemented")
    }

    actual override fun endBackBufferRendering(backBufferId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun endRendering(view: ViewData) {
        TODO("Not yet implemented")
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
        TODO("Not yet implemented")
    }

    actual override fun getScreenshotPixels(area: Vector4i): ByteArray {
        TODO("Not yet implemented")
    }

    actual override fun setActiveShader(shaderId: Int) {
        TODO("Not yet implemented")
    }


}