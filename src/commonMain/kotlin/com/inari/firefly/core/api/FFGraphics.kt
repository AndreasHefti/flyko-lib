package com.inari.firefly.core.api

import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector4i

/** This defines the low level API-Interface for all graphical functions used by the firefly API.
 *
 * There are two kinds of Views; The BaseView that always is the Window-View. No matter if there are more Views available, the BaseView is always existing.
 * Any number of VirtualViews. A VirtualView is a rectangular region within the BaseView that defines its own 2D space (camera/view).
 * Usually a VirtualView can be implemented within Viewports or FBO's within rendering to textures on GPU level.
 * For more Information about Views see ViewSystem
 *
 */
expect object FFGraphics {

    /** Use this to get the actual screen width
     * @return the actual screen width
     */
    val screenWidth: Int

    /** Use this to get the actual screen height
     * @return the actual screen height
     */
    val screenHeight: Int

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

    /** This is called from firefly API whenever a frame-buffer is created and a low-level FBO (for example)
     * id needed to represent this back buffer.
     *
     * @param data [FrameBufferData] defining all properties for the low-level representation (FBO)
     * @return An instance identifier of the low-level frame-buffer representation
     */
    fun createFrameBuffer(data: FrameBufferData): Int

    /** This is called by the firefly API whenever a given and loaded frame-buffer is disposed.
     * This shall release all low-level API representation data for the frame-buffer (FBO for example)
     *
     * @param frameBufferId The instance identifier of the low-level frame-buffer representation
     */
    fun disposeFrameBuffer(frameBufferId: Int)

    /** This is called form the firefly API before rendering to a given [ViewData] and must
     * prepare all the stuff needed to render the that [ViewData] on following renderXXX calls.
     *
     * @param view the [ViewData] that is starting to be rendered
     * @param clear indicates whether the [ViewData] should be cleared with the vies clear-color before rendering or not
     */
    fun startViewportRendering(view: ViewData, clear: Boolean)

    /** This is called form the firefly API before rendering to a given back-buffer (FBO) and must
     * prepare all the stuff needed to render the that back-buffer on following renderXXX calls.
     *
     * @param frameBufferId the instance id of the previous created [FrameBufferData] that is starting to be rendered
     * @param posX the world (rendering) position of the frame-buffers upper left corner
     * @param posY the world (rendering) position of the frame-buffers upper left corner
     * @param clear indicates whether the frame-buffer should be cleared with the vies clear-color before rendering or not
     */
    fun startFrameBufferRendering(frameBufferId: Int, posX: Int, posY: Int, clear: Boolean)

    /** Set the active rendering shader. Note that the shader program must have been created before with createShader.
     * @param shaderId The instance identifier of the shader.
     */
    fun setActiveShader(shaderId: Int)

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

    /** This is called form the firefly API to notify the end of rendering for a specified frame-buffer.
     * The graphics context shall unbind the current frame-buffer and bind the actual [ViewData] again
     * for further rendering.
     *
     * @param frameBufferId the instance id of the previous created [FrameBufferData] that is starting to be rendered
     */
    fun endFrameBufferRendering(frameBufferId: Int)

    /** This is called form the firefly API to notify the end of rendering for a specified [ViewData].
     * @param view [ViewData] that is ending to be rendered
     */
    fun endViewportRendering(view: ViewData)

    fun flush(virtualViews: DynArrayRO<ViewData>)

    fun getScreenshotPixels(area: Vector4i): ByteArray

}
