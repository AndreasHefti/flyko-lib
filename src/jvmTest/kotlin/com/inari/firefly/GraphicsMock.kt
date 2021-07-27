package com.inari.firefly

import com.inari.firefly.core.api.*
import com.inari.firefly.core.component.CompId
import com.inari.firefly.graphics.view.ViewEvent
import com.inari.firefly.graphics.view.ViewEvent.Type.*
import com.inari.util.Consumer
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Rectangle

object GraphicsMock : GraphicsAPI {

    val _loadedAssets: DynArray<String> = DynArray.of(20, 10)
    val _views = ArrayList<CompId>()

    val _log = ArrayList<String>()

    override val screenWidth: Int
        get() = 100

    override val screenHeight: Int
        get() = 100

    private val viewListener: Consumer<ViewEvent> = { event ->
        when (event.type) {
            VIEW_CREATED -> _views.add(event.id)
            VIEW_DELETED -> _views.remove(event.id)
            else -> {}
        }
    }

    init {
        FFContext.registerListener(ViewEvent, viewListener)
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
        return _loadedAssets.add("sprite: ${data.textureId} : ${data.region}")
    }

    override fun disposeSprite(spriteId: Int) {
        _loadedAssets.remove(spriteId)
    }

    override fun createShader(data: ShaderData): Int {
        return _loadedAssets.add(data.name)
    }

    override fun disposeShader(shaderId: Int) {
        _loadedAssets.remove(shaderId)
    }



    override fun startRendering(view: ViewData, clear: Boolean) {
        _log.add("startRendering::$view")
    }

    override fun renderSprite(renderableSprite: SpriteRenderable, xpos: Float, ypos: Float) {
        _log.add("renderSprite::Sprite($renderableSprite)")
    }

    override fun renderSprite(renderableSprite: SpriteRenderable, xpos: Float, ypos: Float, scale: Float) {
        _log.add("renderSprite::Sprite($renderableSprite)")
    }

    override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData) {
        _log.add("renderSprite::Sprite($renderableSprite)")
    }

    override fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
        _log.add("renderShape:: $data")
    }

    override fun renderShape(data: ShapeData, transform: TransformData) {
        _log.add("renderShape:: $data : $transform")
    }

    override fun endRendering(view: ViewData) {
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

    override fun getScreenshotPixels(area: Rectangle): ByteArray {
        null!!
    }

}
