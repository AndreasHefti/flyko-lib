package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i

actual object GraphicsAPIImpl : GraphicsAPI {

    actual override val screenWidth: Int
        get() = TODO("Not yet implemented")

    actual override val screenHeight: Int
        get() = TODO("Not yet implemented")

    actual override fun createView(viewData: ViewData) {
        TODO("Not yet implemented")
    }

    actual override fun disposeView(viewId: Int) {
        TODO("Not yet implemented")
    }

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

    actual override fun setActiveShader(shaderId: Int) {
        TODO("Not yet implemented")
    }

    actual override fun clearView(view: ViewData) {
        TODO("Not yet implemented")
    }

    actual override fun startViewportRendering(view: ViewData, clear: Boolean) {
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

    actual override fun endViewportRendering(view: ViewData) {
        TODO("Not yet implemented")
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
        TODO("Not yet implemented")
    }

    actual override fun getTexturePixels(textureId: Int): ByteArray {
        TODO("Not yet implemented")
    }

    actual override fun setTexturePixels(textureId: Int, region: Vector4i, pixels: ByteArray) {
        TODO("Not yet implemented")
    }

    actual override fun getScreenshotPixels(region: Vector4i): ByteArray {
        TODO("Not yet implemented")
    }

    actual override fun applyViewportOffset(x: Float, y: Float) {
        TODO("Not yet implemented")
    }

}