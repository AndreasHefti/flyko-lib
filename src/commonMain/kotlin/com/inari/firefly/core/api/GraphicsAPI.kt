package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i

/** This defines the low level API-Interface for all graphical functions used by the firefly API.
 *
 * There are two kinds of Views; The BaseView that always is the Window-View. No matter if there are more Views available, the BaseView is always existing.
 * Any number of VirtualViews. A VirtualView is a rectangular region within the BaseView that defines its own 2D space (camera/view).
 * Usually a VirtualView can be implemented within Viewports or FBO's within rendering to textures on GPU level.
 * For more Information about Views see ViewSystem
 *
 */
interface GraphicsAPI {

    /** Use this to get the actual screen width
     * @return the actual screen width
     */
    val screenWidth: Int

    /** Use this to get the actual screen height
     * @return the actual screen height
     */
    val screenHeight: Int

    /** This creates a new viewport with the given ViewData.
     * Uses the view data index for identifier
     * @param viewData The ViewData
     */
    fun createView(viewData: ViewData)

    /** Dispose the viewport with the given identifier
     * @param viewId The ViewData identifier (index) if the viewport to dispose
     */
    fun disposeView(viewId: Int)

    /** This is called from the firefly API when a texture is created/loaded and should be loaded into the GPU
     *
     * @param data The texture DAO
     * @return the texture identifier to identify the texture on lower level API and
     *         the width and height of the texture within a Triple
     */
    fun createTexture(data: TextureData): Triple<Int, Int, Int>

    /** This is called from the firefly API when a texture is disposed and should be deleted from GPU.
     * and must release and delete the texture on GPU level
     *
     * @param textureId identifier of the texture to dispose.
     */
    fun disposeTexture(textureId: Int)

    /** This is called from the firefly API when a sprite is created/loaded and gives an identifier for that sprite.
     *
     * @param data the sprite DAO
     * @return the sprite identifier to identify the sprite on lower level API.
     */
    fun createSprite(data: SpriteData): Int

    /** This is called from the firefly API when a sprite is disposed
     * and must release and delete the sprite on lower level
     *
     * @param spriteId the sprite identifier of the texture to dispose.
     */
    fun disposeSprite(spriteId: Int)

    /** This is called from the firefly API when a shader script is created/loaded and gives an
     * identifier for that shader script.
     *
     * @param data the shader DAO
     * @return the shader identifier to identify the shader on lower level API.
     */
    fun createShader(data: ShaderData): Int

    /** This is called from the firefly API when a shader script is disposed
     * and must release and delete the shader script on GPU level
     *
     * @param shaderId identifier of the shader to dispose.
     */
    fun disposeShader(shaderId: Int)

    /** Set the active sprite rendering shader. Note that the shader program must have been created before with createShader.
     * @param shaderId The instance identifier of the shader.
     */
    fun setActiveShader(shaderId: Int)

    /** This is called form the firefly API before rendering to a given [ViewData] and must
     * prepare all the stuff needed to render the that [ViewData] on following renderXXX calls.
     *
     * @param view the [ViewData] that is starting to be rendered
     * @param clear indicates whether the [ViewData] should be cleared with the vies clear-color before rendering or not
     */
    fun startViewportRendering(view: ViewData, clear: Boolean)

    /** This is called form the firefly API to render a created texture on specified position to the actual [ViewData]
     *
     * @param textureId the texture identifier
     * @param posX the x-axis offset
     * @param posY the y-axis offset
     * @param tintColor the tint color for alpha blending. Default is Vector4f(1f, 1f, 1f, 1f)
     * @param blendMode the blend mode. Default is BlendMode.NONE
     */
    fun renderTexture(
        textureId: Int,
        posX: Float,
        posY: Float,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        rotation: Float = 0f,
        flipX: Boolean = false,
        flipY: Boolean = false,
        tintColor: Vector4f = Vector4f(1f, 1f, 1f, 1f),
        blendMode: BlendMode = BlendMode.NONE)

    /** This is called form the firefly API to render a created sprite on specified position to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param xOffset the x-axis offset
     * @param yOffset the y-axis offset
     */
    fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float)

    /** This is called form the firefly API to render a created sprite with specified [TransformData] to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param transform [TransformData] DAO containing all transform data to render the sprite like: position-offset, scale, pivot, rotation
     */
    fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData)

    /** This is called form the firefly API to render a created sprite with specified [TransformData] to the actual [ViewData]
     *
     * @param renderableSprite the sprite DAO
     * @param transform [TransformData] DAO containing all transform data to render the sprite like: position-offset, scale, pivot, rotation
     * @param xOffset the x-axis offset
     * @param yOffset the y-axis offset
     */
    fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float)

    /** This is called form the firefly API to render a shape. See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param xOffset the x-axis offset, default is 0f
     * @param yOffset the y-axis offset, default is 0f
     */
    fun renderShape(data: ShapeData, xOffset: Float = 0f, yOffset: Float = 0f)

    /** This is called form the firefly API to render a shape with given [TransformData].
     * See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param transform [TransformData] DAO
     */
    fun renderShape(data: ShapeData, transform: TransformData)

    /** This is called form the firefly API to render a shape with given [TransformData].
     * See [ShapeData] for more information about the data structure of shapes.
     *
     * @param data [ShapeData] DAO
     * @param transform [TransformData] DAO
     * @param xOffset the x-axis offset
     * @param yOffset the y-axis offset
     */
    fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float)

    /** This is called form the firefly API to notify the end of rendering for a specified [ViewData].
     * @param view [ViewData] that is ending to be rendered
     */
    fun endViewportRendering(view: ViewData)

    fun flush(virtualViews: DynArrayRO<ViewData>)

    fun getScreenshotPixels(area: Vector4i): ByteArray

}

expect object GraphicsAPIImpl {
    val screenWidth: Int
    val screenHeight: Int
    fun createView(viewData: ViewData)
    fun disposeView(viewId: Int)
    fun createTexture(data: TextureData): Triple<Int, Int, Int>
    fun disposeTexture(textureId: Int)
    fun createSprite(data: SpriteData): Int
    fun disposeSprite(spriteId: Int)
    fun createShader(data: ShaderData): Int
    fun disposeShader(shaderId: Int)
    fun startViewportRendering(view: ViewData, clear: Boolean)
    fun setActiveShader(shaderId: Int)
    fun renderTexture(textureId: Int, posX: Float, posY: Float, scaleX: Float = 1f, scaleY: Float = 1f, rotation: Float = 0f, flipX: Boolean = false, flipY: Boolean = false, tintColor: Vector4f = Vector4f(1f, 1f, 1f, 1f), blendMode: BlendMode = BlendMode.NONE)
    fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float)
    fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData)
    fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float)
    fun renderShape(data: ShapeData, xOffset: Float = 0f, yOffset: Float = 0f)
    fun renderShape(data: ShapeData, transform: TransformData)
    fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float)
    fun endViewportRendering(view: ViewData)
    fun flush(virtualViews: DynArrayRO<ViewData>)
    fun getScreenshotPixels(area: Vector4i): ByteArray
}
