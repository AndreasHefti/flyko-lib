package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Rectangle

actual object FFGraphics {
    /** Use this to get the actual screen width
     * @return the actual screen width
     */
    actual val screenWidth: Int
        get() = TODO("Not yet implemented")

    /** Use this to get the actual screen height
     * @return the actual screen height
     */
    actual val screenHeight: Int
        get() = TODO("Not yet implemented")

    /** This is called from the firefly API when a texture is created/loaded and should be loaded into the GPU
     *
     * @param data The texture DAO
     * @return the texture identifier to identify the texture on lower level API and
     *         the width and height of the texture within a Triple
     */
    actual fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when a texture is disposed and should be deleted from GPU.
     * and must release and delete the texture on GPU level
     *
     * @param textureId identifier of the texture to dispose.
     */
    actual fun disposeTexture(textureId: Int) {
    }

    /** This is called from the firefly API when a sprite is created/loaded and gives an identifier for that sprite.
     *
     * @param data the sprite DAO
     * @return the sprite identifier to identify the sprite on lower level API.
     */
    actual fun createSprite(data: SpriteData): Int {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when a sprite is disposed
     * and must release and delete the sprite on lower level
     *
     * @param spriteId the sprite identifier of the texture to dispose.
     */
    actual fun disposeSprite(spriteId: Int) {
    }

    /** This is called from the firefly API when a shader script is created/loaded and gives an identifier for that shader script.
     *
     * @param data the shader DAO
     * @return the shader identifier to identify the shader on lower level API.
     */
    actual fun createShader(data: ShaderData): Int {
        TODO("Not yet implemented")
    }

    /** This is called from the firefly API when a shader script is disposed
     * and must release and delete the shader script on GPU level
     *
     * @param shaderId identifier of the shader to dispose.
     */
    actual fun disposeShader(shaderId: Int) {
    }

    /** This is called form the firefly API before rendering to a given [ViewData] and must
     * prepare all the stuff needed to render the that [ViewData] on following renderXXX calls.
     *
     * @param view the [ViewData] that is starting to be rendered
     * @param clear indicates whether the [ViewData] should be cleared with the vies clear-color before rendering or not
     */
    actual fun startRendering(view: ViewData, clear: Boolean) {
    }

    /** This is called form the firefly API to render a created sprite on specified position to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param xpos the x-axis position in the 2D world of the actual [ViewData]
     * @param ypos the y-axis position in the 2D world of the actual [ViewData]
     */
    actual fun renderSprite(
        renderableSprite: SpriteRenderable,
        xpos: Float,
        ypos: Float
    ) {
    }

    /** This is called form the firefly API to render a created sprite on specified position and scale to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param xpos the x-axis position in the 2D world of the actual [ViewData]
     * @param ypos the y-axis position in the 2D world of the actual [ViewData]
     * @param scale the x-axis and y-axis scale for the sprite to render
     */
    actual fun renderSprite(
        renderableSprite: SpriteRenderable,
        xpos: Float,
        ypos: Float,
        scale: Float
    ) {
    }

    /** This is called form the firefly API to render a created sprite with specified [TransformData] to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param transform [TransformData] DAO containing all transform data to render the sprite like: position-offset, scale, pivot, rotation
     */
    actual fun renderSprite(
        renderableSprite: SpriteRenderable,
        transform: TransformData
    ) {
    }

    /** This is called form the firefly API to render a shape. See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param xOffset the x-axis offset, default is 0f
     * @param yOffset the y-axis offset, default is 0f
     */
    actual fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
    }

    /** This is called form the firefly API to render a shape with given [TransformData].
     * See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param transform [TransformData] DAO
     */
    actual fun renderShape(
        data: ShapeData,
        transform: TransformData
    ) {
    }

    /** This is called form the firefly API to notify the end of rendering for a specified [ViewData].
     * @param view [ViewData] that is ending to be rendered
     */
    actual fun endRendering(view: ViewData) {
    }

    actual fun flush(virtualViews: DynArrayRO<ViewData>) {
    }

    actual fun getScreenshotPixels(area: Rectangle): ByteArray {
        TODO("Not yet implemented")
    }
}