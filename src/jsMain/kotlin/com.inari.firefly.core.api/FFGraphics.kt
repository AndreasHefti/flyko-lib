package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Rectangle

actual object FFGraphics : GraphicsAPI {
    /** Use this to get the actual screen width
     * @return the actual screen width
     */
    actual override val screenWidth: Int
        get() = TODO("Not yet implemented")

    /** Use this to get the actual screen height
     * @return the actual screen height
     */
    actual override val screenHeight: Int
        get() = TODO("Not yet implemented")

    /** This is called from the firefly API when a texture is created/loaded and should be loaded into the GPU
     *
     * @param data The texture DAO
     * @return the texture identifier to identify the texture on lower level API and
     *         the width and height of the texture within a Triple
     */
    actual override fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when a texture is disposed and should be deleted from GPU.
     * and must release and delete the texture on GPU level
     *
     * @param textureId identifier of the texture to dispose.
     */
    actual override fun disposeTexture(textureId: Int) {
    }

    /** This is called from the firefly API when a sprite is created/loaded and gives an identifier for that sprite.
     *
     * @param data the sprite DAO
     * @return the sprite identifier to identify the sprite on lower level API.
     */
    actual override fun createSprite(data: SpriteData): Int {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when a sprite is disposed
     * and must release and delete the sprite on lower level
     *
     * @param spriteId the sprite identifier of the texture to dispose.
     */
    actual override fun disposeSprite(spriteId: Int) {
    }

    /** This is called from the firefly API when an effect is created/loaded and gives an identifier for that effect script.
     *
     * @param data the shader DAO
     * @return the shader identifier to identify the shader on lower level API.
     */
    actual override fun createEffect(data: EffectData): Int {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when am Effect is disposed
     * and must release and delete all effect related data
     *
     * @param effectId identifier of the effect to dispose.
     */
    actual override fun disposeEffect(effectId: Int) {
        TODO("Not yet implemented")
    }

    /** This is called form the firefly API before rendering to a given [ViewData] and must
     * prepare all the stuff needed to render the that [ViewData] on following renderXXX calls.
     *
     * @param view the [ViewData] that is starting to be rendered
     * @param clear indicates whether the [ViewData] should be cleared with the vies clear-color before rendering or not
     */
    actual override fun startRendering(view: ViewData, clear: Boolean) {
    }

    /** This is called form the firefly API to render a created sprite on specified position to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param xOffset the x-axis offset, default is 0f
     * @param yOffset the y-axis offset, default is 0f
     */
    actual override fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float) {
    }

    /** This is called form the firefly API to render a created sprite with specified [TransformData] to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param transform [TransformData] DAO containing all transform data to render the sprite like: position-offset, scale, pivot, rotation
     */
    actual override fun renderSprite(
        renderableSprite: SpriteRenderable,
        transform: TransformData
    ) {
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float) {
    }

    /** This is called form the firefly API to render a shape. See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param xOffset the x-axis offset, default is 0f
     * @param yOffset the y-axis offset, default is 0f
     */
    actual override fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
    }

    actual override fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float) {

    }

    /** This is called form the firefly API to render a shape with given [TransformData].
     * See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param transform [TransformData] DAO
     */
    actual override fun renderShape(
        data: ShapeData,
        transform: TransformData
    ) {
    }

    /** This is called form the firefly API to notify the end of rendering for a specified [ViewData].
     * @param view [ViewData] that is ending to be rendered
     */
    actual override fun endRendering(view: ViewData) {
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
    }

    actual override fun getScreenshotPixels(area: Rectangle): ByteArray {
        TODO("Not yet implemented")
    }
}