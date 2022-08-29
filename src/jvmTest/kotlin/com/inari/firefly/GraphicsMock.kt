package com.inari.firefly

import com.inari.firefly.core.ComponentEventListener
import com.inari.firefly.core.ComponentEventType
import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.api.*
import com.inari.firefly.graphics.view.View
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i

object GraphicsMock : GraphicsAPI {

    val _loadedAssets: DynArray<String> = DynArray.of(20, 10)
    val _views = ArrayList<ComponentKey>()

    val _log = ArrayList<String>()

    override val screenWidth: Int
        get() = 100

    override val screenHeight: Int
        get() = 100

    override fun createView(viewData: ViewData) {
        _views.add(View.getKey(viewData.index))
    }

    override fun disposeView(viewId: Int) {
        _views.remove(View.getKey(viewId))
    }

    fun clearLogs() {
        _log.clear()
    }

    fun clear() {
        _loadedAssets.clear()
        _views.clear()
        _log.clear()
    }

    override fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        return Triple(_loadedAssets.add(data.resourceName), 0, 0)
    }

    override fun disposeTexture(textureId: Int) {
        _loadedAssets.remove(textureId)
    }

    override fun createSprite(data: SpriteData): Int {
        return _loadedAssets.add("sprite: ${data.textureIndex} : ${data.region}")
    }

    override fun disposeSprite(spriteId: Int) {
        _loadedAssets.remove(spriteId)
    }

    override fun createShader(data: ShaderData): Int {
        return _loadedAssets.add("shader: $data")
    }

    override fun disposeShader(shaderId: Int) {
        _loadedAssets.remove(shaderId)
    }

    override fun createFrameBuffer(data: FrameBufferData): Int {
        return _loadedAssets.add("back-buffer: $data")
    }

    override fun disposeFrameBuffer(frameBufferId: Int) {
        _loadedAssets.remove(frameBufferId)
    }

    override fun setActiveShader(shaderId: Int) {
        TODO("Not yet implemented")
    }

    override fun startViewportRendering(view: ViewData, clear: Boolean) {
        _log.add("startRendering::$view")
    }

    override fun startFrameBufferRendering(frameBufferId: Int, posX: Int, posY: Int, clear: Boolean) {
        TODO("Not yet implemented")
    }

    override fun renderTexture(
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
        _log.add("renderTexture::Sprite($textureId)")
    }

    override fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float) {
        _log.add("renderSprite::Sprite($renderableSprite)")
    }

    override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData) {
        _log.add("renderSprite::Sprite($renderableSprite)")
    }

    override fun renderSprite(
        renderableSprite: SpriteRenderable,
        transform: TransformData,
        xOffset: Float,
        yOffset: Float
    ) {
        _log.add("renderSprite::Sprite($renderableSprite) offset: $xOffset $yOffset")
    }

    override fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
        _log.add("renderShape:: $data")
    }

    override fun renderShape(data: ShapeData, transform: TransformData) {
        _log.add("renderShape:: $data : $transform")
    }

    override fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float) {
        _log.add("renderShape:: $data : $transform  offset: $xOffset $yOffset")
    }

    override fun endFrameBufferRendering(frameBufferId: Int) {
        TODO("Not yet implemented")
    }

    override fun endViewportRendering(view: ViewData) {
        _log.add("endRendering::$view")
    }

    override fun flush(virtualViews: DynArrayRO<ViewData>) {
        _log.add("flush")
    }

    fun loadedAssets(): String {
        return _loadedAssets.toString()
    }

    fun views(): String {
        return _views.toString()
    }

    fun log(): String {
        return _log.toString()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("LowerSystemFacadeMock [loadedAssets=")
        assetsToString(builder)
        builder.append(", views=")
        builder.append(_views)
        builder.append(", log=")
        builder.append(_log)
        builder.append("]")
        return builder.toString()
    }

    private fun assetsToString(builder: StringBuilder) {
        builder.append("[")
        for (assetName in _loadedAssets) {
            builder.append(assetName).append(",")
        }

        if (_loadedAssets.size > 0) {
            builder.deleteAt(builder.length - 1)
        }

        builder.append("]")
    }

    override fun getScreenshotPixels(area: Vector4i): ByteArray {
        null!!
    }

}
