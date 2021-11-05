package com.inari.firefly.core.api

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.Texture.TextureWrap
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.GdxRuntimeException
import com.inari.firefly.*
import com.inari.firefly.graphics.view.ViewEvent
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.firefly.core.api.ShapeType.*
import com.inari.firefly.filter.ColorFilteredTextureData
import com.inari.util.Consumer
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Position
import com.inari.util.geom.PositionF
import com.inari.util.geom.Rectangle
import com.inari.util.graphics.IColor
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL20
import java.nio.ByteBuffer
import java.nio.ByteOrder


actual object FFGraphics : GraphicsAPI {

    private const val DEFAULT_VERTEX_SHADER =
            "attribute vec4 a_position;" +
             "attribute vec4 a_color;" +
             "attribute vec2 a_texCoord0;" +
             "uniform mat4 u_projTrans;" +
             "varying vec4 v_color;" +
             "varying vec2 v_texCoords;" +
             "void main() {" +
             "    v_color = a_color;" +
             "    v_texCoords = a_texCoord0;" +
             "    gl_Position = u_projTrans * a_position;" +
             "}"

    private const val DEFAULT_FRAGMENT_SHADER =
            "#ifdef GL_ES" +
            "precision mediump float;" +
            "#endif" +
            "varying vec4 v_color;" +
            "varying vec2 v_texCoords;" +
            "uniform sampler2D u_texture;" +
            "void main()" +
            "{" +
            "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);" +
            "}"

    private val viewports: DynArray<ViewportData> = DynArray.of(10, 10)
    private val textures: DynArray<Texture> = DynArray.of(30, 50)
    private val fboTextures: DynArray<FBOTexture> = DynArray.of(10, 10)
    private val sprites: DynArray<TextureRegion> = DynArray.of(200, 300)
    private val effects: DynArray<EffectInstanceData> = DynArray.of(5, 10)

    private val spriteBatch = PolygonSpriteBatch()
    private val meshBuilder = PolygonShapeDrawer()
    private var shapeRenderer = ShapeRenderer()

    private var baseViewport: ViewportData? = null
    private var baseView: ViewData? = null

    private var activeViewport: ViewportData? = null
    private var activeEffectId = -1
    private var activeBlend = BlendMode.NONE
    private var activeShapeEffectId = -1

    private val SHAPE_COLOR_1 = Color()
    private val SHAPE_COLOR_2 = Color()
    private val SHAPE_COLOR_3 = Color()
    private val SHAPE_COLOR_4 = Color()

    private val transformMatrix = Matrix4()
    private val vector1 = Vector3()
    private val vector2 = Vector3()
    private val vector3 = Vector3()

    actual override val screenHeight: Int
        get() = Gdx.graphics.height
    actual override val screenWidth: Int
        get() = Gdx.graphics.width

    private val viewEventListener: Consumer<ViewEvent> = { viewEvent ->
        when (viewEvent.type) {
            ViewEvent.Type.VIEW_CREATED -> {
                val viewport: ViewportData
                if (!viewEvent.data.isBase) {
                    viewport = createVirtualViewport(viewEvent.data)
                } else {
                    viewport = createBaseViewport(viewEvent.data)
                    baseViewport = viewport
                    baseView = viewEvent.data
                }
                viewports[viewEvent.id.instanceId] = viewport
            }
            ViewEvent.Type.VIEW_DELETED -> {
                viewports.remove(viewEvent.id.instanceId)?.dispose()
            }
            else -> { }
        }
    }

    init {
        ShaderProgram.pedantic = false
        FFContext.registerListener(ViewEvent, viewEventListener)
    }

    actual override fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        val colorConverter = data.colorConverter

        val texture = if (colorConverter != NULL_INT_FUNCTION)
            Texture(ColorFilteredTextureData(data.resourceName, colorConverter))
        else
            Texture( Gdx.files.internal(data.resourceName), data.isMipmap )

        val textureId = textures.add(texture)

        if ( data.wrapS >= 0 )
            texture.setWrap(getLibGDXTextureWrap(data.wrapS), texture.vWrap)
        if ( data.wrapT >= 0 )
            texture.setWrap(texture.uWrap, getLibGDXTextureWrap(data.wrapT))
        if ( data.minFilter >= 0 )
            texture.setFilter(getLibGDXTextureFilter( data.minFilter), texture.magFilter)
        if ( data.magFilter >= 0 )
            texture.setFilter(texture.minFilter, getLibGDXTextureFilter(data.magFilter))

        return Triple(textureId, texture.width, texture.height)
    }

    actual override fun disposeTexture(textureId: Int) {
        textures.remove(textureId)?.dispose()
    }

    actual override fun createSprite(data: SpriteData): Int {
        if (data.textureId !in textures)
            throw IllegalStateException("Texture with id: ${data.textureId} not loaded" )

        val texture = textures[data.textureId]
        val sprite = TextureRegion(
            texture,
            data.region.x,
            data.region.y,
            data.region.width,
            data.region.height
        )

        sprite.flip(data.isHorizontalFlip, !data.isVerticalFlip)
        return sprites.add(sprite)
    }

    actual override fun disposeSprite(spriteId: Int) {
        sprites.remove(spriteId)
    }

    actual override fun createEffect(data: EffectData): Int {
        var vertexShader = data.shaderData.vertexShaderProgram
        var fragmentShader = data.shaderData.fragmentShaderProgram

        if (vertexShader == NO_PROGRAM) {
            // try to load from resource
            vertexShader = try {
                Gdx.files.internal(data.shaderData.vertexShaderResourceName).readString()
            } catch (e: Exception) {
                System.err.println("Failed to load vertex shader from resource: ${data.shaderData.vertexShaderResourceName}")
                println("Use default vertex shader")
                DEFAULT_VERTEX_SHADER
            }
        }

        if (fragmentShader == NO_PROGRAM) {
            fragmentShader = try {
                Gdx.files.internal(data.shaderData.fragmentShaderResourceName).readString()
            } catch (e: Exception) {
                System.err.println("Failed to load fragment shader from resource: ${data.shaderData.fragmentShaderResourceName}")
                println("Use default fragment shader")
                DEFAULT_FRAGMENT_SHADER
            }
        }

        val shaderProgram = ShaderProgram(vertexShader, fragmentShader)
        if (shaderProgram.isCompiled)
            println("Shader Compiled: ${shaderProgram.log}")
        else
            throw RuntimeException("ShaderData failed to compile:" + shaderProgram.log)

        val effectDataInstance =  EffectInstanceData(
            shaderProgram,
            data.textureBindings.toArray(),
            data.fboTextureBindings.map{ createFBOTexture(it) }.toTypedArray()
        )

        return effects + effectDataInstance
    }

    actual override fun disposeEffect(effectId: Int) {
        effects.remove(effectId)?.dispose()
    }

    actual override fun startRendering(view: ViewData, clear: Boolean) {
        activeViewport = viewports[view.index]
        activeViewport?.activate(spriteBatch, shapeRenderer, view, clear)
        spriteBatch.begin()
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float) {
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        val sprite = sprites[renderableSprite.spriteId]
        setEffect(renderableSprite.effectInstanceRef)

        spriteBatch.draw(sprite, xOffset, yOffset)
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData) {
        val sprite = sprites[renderableSprite.spriteId] ?: return
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        setEffect(renderableSprite.effectInstanceRef)
        spriteBatch.draw(
            sprite,
            transform.position.x,
            transform.position.y,
            transform.pivot.x,
            transform.pivot.y,
            sprite.regionWidth.toFloat(),
            sprite.regionHeight.toFloat(),
            transform.scale.dx,
            transform.scale.dy,
            transform.rotation
        )
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float) {
        val sprite = sprites[renderableSprite.spriteId] ?: return
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        setEffect(renderableSprite.effectInstanceRef)
        spriteBatch.draw(
            sprite,
            transform.position.x + xOffset,
            transform.position.y + yOffset,
            transform.pivot.x,
            transform.pivot.y,
            sprite.regionWidth.toFloat(),
            sprite.regionHeight.toFloat(),
            transform.scale.dx,
            transform.scale.dy,
            transform.rotation
        )
    }

    actual override fun renderShape(data: ShapeData, xOffset: Float, yOffset: Float) {
        if (doRenderWithShapeRenderer(data)) {
            renderWithShapeRenderer(data, xOffset, yOffset)
            return
        }

        getShapeColor(data.color1, SHAPE_COLOR_1)
        getShapeColor(data.color2 ?: data.color1, SHAPE_COLOR_2)
        getShapeColor(data.color3 ?: data.color1, SHAPE_COLOR_3)
        getShapeColor(data.color4 ?: data.color1, SHAPE_COLOR_4)

        setColorAndBlendMode(data.color1, data.blend)
        setEffect(data.effectInstanceRef)

        meshBuilder.setColor(SHAPE_COLOR_1)

        val type = data.type
        var index = 0

        when (type) {
            POLYGON     -> throw UnsupportedOperationException() //meshBuilder.vertex(*data.vertices)
            RECTANGLE   -> while (index < data.vertices.size) {
                val x = data.vertices[index++] + xOffset
                val y = data.vertices[index++] + yOffset
                val width = data.vertices[index++]
                val height = data.vertices[index++]
                meshBuilder.rect(
                        x, y, 0.0f,
                        x + width, y, 0.0f,
                        x + width, y + height, 0.0f,
                        x, y + height, 0.0f,
                        1f, 1f, 1f)
            }
            CIRCLE     -> while (index < data.vertices.size) {
                val radius = data.vertices[index++]
                val xpos = data.vertices[index++] + xOffset
                val ypos = data.vertices[index++] + yOffset
                EllipseShapeBuilder.build(meshBuilder, radius, data.segments, xpos, ypos, 0f, 0f, 0f, 1f)
            }
            TRIANGLE     -> while (index < data.vertices.size) {
                meshBuilder.triangle(
                        vector1.set(data.vertices[index++] + xOffset, data.vertices[index++] + yOffset, 0f),
                        SHAPE_COLOR_2,
                        vector2.set(data.vertices[index++] + xOffset, data.vertices[index++] + yOffset, 0f),
                        SHAPE_COLOR_3,
                        vector3.set(data.vertices[index++] + xOffset, data.vertices[index++] + yOffset, 0f),
                        SHAPE_COLOR_4)
            }
            else -> throw java.lang.IllegalStateException()
        }

        meshBuilder.draw(spriteBatch)
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    actual override fun renderShape(data: ShapeData, transform: TransformData) =
        renderShape(data, transform, 0.0f, 0.0f)


    actual override fun renderShape(data: ShapeData, transform: TransformData, xOffset: Float, yOffset: Float) {

        if (doRenderWithShapeRenderer(data)) {
            renderWithShapeRenderer(data, transform)
            return
        }

        transformMatrix.idt()

        if (transform.hasScale) {
            transformMatrix.translate(transform.pivot.x, transform.pivot.y, 0f)
            transformMatrix.scale(transform.scale.dx, transform.scale.dy, 0f)
            transformMatrix.translate(-transform.pivot.x, -transform.pivot.y, 0f)
            spriteBatch.transformMatrix = transformMatrix
        }

        if (transform.hasRotation) {
            transformMatrix.translate(transform.pivot.x, transform.pivot.y, 0f)
            transformMatrix.rotate(0f, 0f, 1f, transform.rotation)
            transformMatrix.translate(-transform.pivot.x, -transform.pivot.y, 0f)
            spriteBatch.transformMatrix = transformMatrix
        }

        renderShape(data, transform.position.x + xOffset, transform.position.y + yOffset)

        if (transform.hasScale || transform.hasRotation) {
            transformMatrix.idt()
            spriteBatch.transformMatrix = transformMatrix
        }
    }

    private fun doRenderWithShapeRenderer(data: ShapeData): Boolean =
            data.type === LINE ||
                    data.type === POINT ||
                    data.type === POLY_LINE ||
                    data.type === ARC ||
                    data.type === CURVE || !data.fill


    private fun renderWithShapeRenderer(data: ShapeData, xOffset: Float, yOffset: Float) {
        getShapeColor(data.color1, SHAPE_COLOR_1)
        getShapeColor(data.color2 ?: data.color1, SHAPE_COLOR_2)
        getShapeColor(data.color3 ?: data.color1, SHAPE_COLOR_3)
        getShapeColor(data.color4 ?: data.color1, SHAPE_COLOR_4)

        if (data.effectInstanceRef != activeShapeEffectId)
            if (data.effectInstanceRef < 0)
                shapeRenderer = ShapeRenderer()
            else {
                val effect = effects[data.effectInstanceRef]
                if (effect != null) {
                    effect.activate(true)
                    shapeRenderer = ShapeRenderer(1000, effect.program)
                }
            }
        activeShapeEffectId = data.effectInstanceRef

        shapeRenderer.color = SHAPE_COLOR_1
        var restartSpriteBatch = false
        if (spriteBatch.isDrawing) {
            restartSpriteBatch = true
            spriteBatch.end()
        }

        val type = data.type
        val shapeType = when {
            type === POINT  -> ShapeRenderer.ShapeType.Point
            else            -> ShapeRenderer.ShapeType.Line
        }
        shapeRenderer.begin(shapeType)

        val blendMode = data.blend
        val vertices = data.vertices
        val segments = data.segments
        if (blendMode !== BlendMode.NONE) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendColor(1f, 1f, 1f, 1f)
            Gdx.gl.glBlendFunc(blendMode.source, blendMode.dest)
        }

        var index = 0
        when (data.type) {
            POINT       -> while (index < vertices.size) {
                shapeRenderer.point(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        0f)
            }
            LINE        -> while (index < vertices.size) {
                shapeRenderer.line(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        SHAPE_COLOR_1,
                        SHAPE_COLOR_2)
            }
            POLY_LINE   -> {
                shapeRenderer.translate(xOffset, yOffset, 0f)
                shapeRenderer.polyline(vertices)
                shapeRenderer.translate(-xOffset, -yOffset, 0f)
            }
            POLYGON     -> {
                shapeRenderer.translate(xOffset, yOffset, 0f)
                shapeRenderer.polygon(vertices)
                shapeRenderer.translate(-xOffset, -yOffset, 0f)
            }
            RECTANGLE   -> while (index < vertices.size) {
                shapeRenderer.rect(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++],
                        vertices[index++],
                        SHAPE_COLOR_1,
                        SHAPE_COLOR_2,
                        SHAPE_COLOR_3,
                        SHAPE_COLOR_4)
            }
            CIRCLE      -> while (index < vertices.size) {
                val radius = data.vertices[index++]
                val xpos = data.vertices[index++] + xOffset
                val ypos = data.vertices[index++] + yOffset
                shapeRenderer.circle(xpos, ypos, radius, segments)
            }
            ARC         -> while (index < vertices.size) {
                shapeRenderer.arc(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++],
                        vertices[index++],
                        vertices[index++],
                        segments)
            }
            CURVE       -> while (index < vertices.size) {
                shapeRenderer.curve(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        segments
                )
            }
            TRIANGLE -> while (index < vertices.size) {
                shapeRenderer.triangle(
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        vertices[index++] + xOffset,
                        vertices[index++] + yOffset,
                        SHAPE_COLOR_1,
                        SHAPE_COLOR_2,
                        SHAPE_COLOR_3
                )
            }
        }

        shapeRenderer.flush()
        shapeRenderer.end()

        if (restartSpriteBatch)
            spriteBatch.begin()

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun renderWithShapeRenderer(data: ShapeData, transform: TransformData) {
        shapeRenderer.identity()

        if (transform.hasScale) {
            shapeRenderer.translate(transform.pivot.x, transform.pivot.y, 0f)
            shapeRenderer.scale(transform.scale.dx, transform.scale.dy, 0f)
            shapeRenderer.translate(-transform.pivot.x, -transform.pivot.y, 0f)
        }

        if (transform.hasRotation) {
            shapeRenderer.translate(transform.pivot.x, transform.pivot.y, 0f)
            shapeRenderer.rotate(0f, 0f, 1f, transform.rotation)
            shapeRenderer.translate(-transform.pivot.x, -transform.pivot.y, 0f)
        }

        renderWithShapeRenderer(data, transform.position.x, transform.position.y)
        shapeRenderer.identity()
    }

    actual override fun endRendering(view: ViewData) {
        spriteBatch.flush()
        if (!view.isBase )
            activeViewport?.fbo?.end()
        spriteBatch.end()
        activeViewport = null
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
        if (!virtualViews.isEmpty) {
            baseViewport?.activate(spriteBatch, shapeRenderer, baseView!!, true)
            spriteBatch.begin()

            var i = 0
            while (i < virtualViews.capacity) {
                val virtualView = virtualViews[i++] ?: continue
                val viewport = viewports[virtualView.index] ?: continue
                val bounds = virtualView.bounds
                setColorAndBlendMode(virtualView.tintColor, virtualView.blendMode)
                setEffect(virtualView.effectInstanceRef)

                spriteBatch.draw(
                    viewport.fboTexture,
                    bounds.pos.x.toFloat(), bounds.pos.y.toFloat(),
                    bounds.width.toFloat(), bounds.height.toFloat()
                )
            }
            spriteBatch.end()
        }

        spriteBatch.flush()
        activeBlend = BlendMode.NONE
    }


    actual override fun getScreenshotPixels(area: Rectangle): ByteArray {
        val flippedY = screenHeight - area.height + area.pos.y
        val size = area.width * area.height * 3
        val screenContents = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN)
        GL11.glReadPixels(area.pos.x, flippedY, area.width, area.height, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, screenContents)
        val array = ByteArray(size)
        val inverseArray = ByteArray(size)
        screenContents.get(array)

        var y = 0
        while (y < area.height) {
            System.arraycopy(
                array,
                GeomUtils.getFlatArrayIndex(0, area.height - y - 1, area.width * 3),
                inverseArray,
                GeomUtils.getFlatArrayIndex(0, y, area.width * 3),
                area.width * 3
            )
            y++
        }

        return inverseArray
    }

    private fun createBaseViewport(viewPort: ViewData): ViewportData {
        return ViewportData(
            OrthographicCamera(
                viewPort.bounds.width.toFloat(),
                viewPort.bounds.height.toFloat()
            ),
            null, null
        )
    }

    private fun createVirtualViewport(viewPort: ViewData): ViewportData {
        val camera = OrthographicCamera(
            viewPort.bounds.width.toFloat(),
            viewPort.bounds.height.toFloat()
        )
        val frameBuffer = FrameBuffer(
            Pixmap.Format.RGBA8888,
            (viewPort.bounds.width * viewPort.fboScale).toInt(),
            (viewPort.bounds.height * viewPort.fboScale).toInt(),
            false
        )
        val textureRegion = TextureRegion(frameBuffer.colorBufferTexture)
        textureRegion.flip(false, false)

        return ViewportData(camera, frameBuffer, textureRegion)
    }

    private fun createFBOTexture(binding: FBOTextureBind): TextureBind {
        val instanceId = createFBOTexture(binding.width, binding.height, binding.clearColor)
        return TextureBind(binding.bindingName, instanceId)
    }

    private fun createFBOTexture(width: Int, height: Int, clearColor: IColor): Int {
        val fboTex = FBOTexture(width, height, Color(clearColor.r, clearColor.g, clearColor.b, clearColor.a))
        return fboTextures + fboTex
    }

    private class ViewportData constructor(
        val camera: OrthographicCamera,
        val fbo: FrameBuffer?,
        val fboTexture: TextureRegion?) {

         fun activate(
            spriteBatch: PolygonSpriteBatch,
            shapeRenderer: ShapeRenderer,
            view: ViewData,
            clear: Boolean
        ) {
            val worldPosition = view.worldPosition
            val zoom = view.zoom
            val clearColor = view.clearColor
            val bounds = view.bounds

            camera.setToOrtho(true, bounds.width * zoom, bounds.height * zoom)
            camera.position.x = camera.position.x + worldPosition.x
            camera.position.y = camera.position.y + worldPosition.y
            camera.update()
            spriteBatch.projectionMatrix = camera.combined
            shapeRenderer.projectionMatrix = camera.combined

            fbo?.begin()

            if (clear) {
                Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
            }
        }

        fun dispose() = fbo?.dispose()
    }

    private fun getLibGDXTextureWrap(glConst: Int): TextureWrap =
        TextureWrap.values().firstOrNull { it.glEnum == glConst }
            ?: TextureWrap.Repeat

    private fun getLibGDXTextureFilter(glConst: Int): TextureFilter =
        TextureFilter.values().firstOrNull { it.glEnum == glConst }
            ?: TextureFilter.Linear

    private fun setColorAndBlendMode(renderColor: IColor, blendMode: BlendMode) {
        spriteBatch.setColor(renderColor.r, renderColor.g, renderColor.b, renderColor.a)
        if (activeBlend !== blendMode) {
            activeBlend = blendMode
            if (activeBlend !== BlendMode.NONE) {
                spriteBatch.enableBlending()
                spriteBatch.setBlendFunction(activeBlend.source, activeBlend.dest)
            } else
                spriteBatch.disableBlending()
        }
    }

    private fun setEffect(effectId: Int) {
        if (effectId != activeEffectId) {
            spriteBatch.flush()
            if (effectId < 0) {
                spriteBatch.shader = null
                activeEffectId = -1
            } else {
                val effectInstance = effects[effectId]
                if (effectInstance != null) {
                    effectInstance.activate(true)
                    activeEffectId = effectId
                }
            }
        }
    }

    private fun getShapeColor(rgbColor: IColor, color: Color) =
        color.set(rgbColor.r, rgbColor.g, rgbColor.b, rgbColor.a)

    class PolygonShapeDrawer : MeshBuilder() {
        private val texture: Texture

        init {
            super.begin(
                    VertexAttributes(
                        VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                        VertexAttribute.ColorPacked(),
                        VertexAttribute.TexCoords(0)),
                    GL20.GL_TRIANGLES)

            val pixMap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
            pixMap.setColor(1f, 1f, 1f, 1f)
            pixMap.fill()
            texture = Texture(pixMap)
            pixMap.dispose()
        }
        override fun end(): Mesh {
            throw GdxRuntimeException("Not supported!")
        }

        override fun end(mesh: Mesh?): Mesh {
            throw GdxRuntimeException("Not supported!")
        }

        fun draw(batch: PolygonSpriteBatch) {
            batch.draw(texture, vertices, 0, numVertices * floatsPerVertex, indices, 0, numIndices)
            clear()
        }
    }

    private class EffectInstanceData (
        val program: ShaderProgram,
        val textureArray: Array<TextureBind> = emptyArray(),
        val fboTextureArray: Array<TextureBind> = emptyArray(),
        val shaderInit: ShaderInit = {}
    ) {
        fun activate(clear: Boolean = true) {
            spriteBatch.shader = program
            shaderInit(GDXShaderInitAdapter(program))

            var texNum = 1
            textureArray.forEach { bindTexture(it, texNum++) }
            fboTextureArray.forEach { fboTextures[it.textureId]?.bindToShader(program, it.bindingName, texNum++) }

            // (re)activate default texture
            Gdx.graphics.gL20.glActiveTexture(GL20.GL_TEXTURE0)
        }

        private fun bindTexture(texBind: TextureBind, num: Int) {
            program.setUniformi(texBind.bindingName, num)
            textures[texBind.textureId]?.bind(1)
        }

        fun dispose() {
            fboTextureArray.forEach { fboTextures[it.textureId]?.dispose() }
        }
    }

    private class GDXShaderInitAdapter(val program: ShaderProgram) : ShaderInitAdapter {
        override fun setUniformFloat(name: String, value: Float) = program.setUniformf(name, value)
        override fun setUniformVec2(name: String, position: PositionF) = program.setUniformf(name, position.x, position.y)
        override fun setUniformVec2(name: String, position: Position) = program.setUniformf(name, position.x.toFloat(), position.y.toFloat())
        override fun setUniformColorVec4(name: String, color: IColor) = program.setUniformf(name, color.r, color.g, color.b, color.a)
    }

    private class FBOTexture(
        width: Int,
        height: Int,
        val clearColor: Color) {

        val frameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)
        val textureRegion = TextureRegion(frameBuffer.colorBufferTexture)

        fun activate(clear: Boolean) {
            textureRegion.flip(false, false)
            frameBuffer.begin()

            if (clear) {
                Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
            }
        }

        fun bindToShader(program: ShaderProgram, binding: String, num: Int) {
            program.setUniformi(binding, num)
            textureRegion.texture.bind(num)
        }

        fun dispose() = frameBuffer.dispose()
    }
}