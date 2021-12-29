package com.inari.firefly.core.api

import com.inari.firefly.BlendMode
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4f
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
        TODO("Not yet implemented")
    }

    actual override fun createFrameBuffer(data: FrameBufferData): Int {
        TODO("Not yet implemented")
    }

    actual override fun disposeFrameBuffer(frameBufferId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun setActiveShader(shaderId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun startViewportRendering(view: ViewData, clear: Boolean) {
        TODO("Not yet implemented")
    }

    actual override fun startFrameBufferRendering(frameBufferId: Int, posX: Int, posY: Int, clear: Boolean) {
        TODO("Not yet implemented")
    }

    actual override fun renderTexture(
        textureId: Int,
        posX: Float,
        posY: Float,
        scaleX: Float,
        scaleY: Float,
        rotation: Float,
        flipX: Boolean,
        flipY: Boolean,
        tintColor: Vector4f,
        blendMode: BlendMode
    ) {
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

    actual override fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float) {
        TODO("Not yet implemented")
    }

    actual override fun renderShape(data: ShapeData, transform: TransformData) {
        TODO("Not yet implemented")
    }

    actual override fun endFrameBufferRendering(frameBufferId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun endViewportRendering(view: ViewData) {
        TODO("Not yet implemented")
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
        TODO("Not yet implemented")
    }

    actual override fun getScreenshotPixels(area: Vector4i): ByteArray {
        TODO("Not yet implemented")
    }

}