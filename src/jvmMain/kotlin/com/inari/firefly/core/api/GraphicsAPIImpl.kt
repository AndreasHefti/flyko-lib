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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.GdxRuntimeException
import com.inari.firefly.core.api.ShapeType.*
import com.inari.firefly.filter.ColorFilteredTextureData
import com.inari.firefly.graphics.view.View
import com.inari.util.INT_FUNCTION_IDENTITY
import com.inari.util.NO_NAME
import com.inari.util.NO_PROGRAM
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL20
import java.nio.ByteBuffer
import java.nio.ByteOrder


actual object GraphicsAPIImpl : GraphicsAPI {

    const val DEFAULT_VERTEX_SHADER =
            "attribute vec4 a_position;\n" +
             "attribute vec4 a_color;\n" +
             "attribute vec2 a_texCoord0;\n" +
             "uniform mat4 u_projTrans;\n" +
             "varying vec4 v_color;\n" +
             "varying vec2 v_texCoords;\n" +
             "void main() {\n" +
             "    v_color = a_color;\n" +
             "    v_texCoords = a_texCoord0;\n" +
             "    gl_Position = u_projTrans * a_position;\n" +
             "}\n"

    const val DEFAULT_FRAGMENT_SHADER =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main()\n" +
            "{\n" +
            "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
            "}\n"

    private val defaultShaderProgram = ShaderProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER)

    private val viewports: DynArray<ViewportFBO> = DynArray.of(10, 10)
    private val textures: DynArray<Texture> = DynArray.of(30, 50)
    private val shaders: DynArray<Shader> = DynArray.of(5, 5)
    private val sprites: DynArray<TextureRegion> = DynArray.of(200, 300)

    private val spriteBatch = PolygonSpriteBatch()
    private val meshBuilder = PolygonShapeDrawer()
    private var shapeRenderer = ShapeRenderer()

    private var baseViewport: ViewportFBO? = null
    private var baseView: ViewData? = null

    private var activeViewportId: Int = -1
    private var activeShaderId = -1
    private var activeBackBufferId = -1
    private var activeBlend = BlendMode.NONE
    private var activeShapeShaderId = -1

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

    init {
        ShaderProgram.pedantic = false
    }

    actual override fun createView(viewData: ViewData) {
        val viewport: ViewportFBO
        if (!viewData.isBase) {
            viewport = createVirtualViewport(viewData)
        } else {
            viewport = createBaseViewport(viewData)
            baseViewport = viewport
            baseView = viewData
        }
        viewports[viewData.index] = viewport
    }

    actual override fun disposeView(viewId: Int) {
        if (viewId != View.BASE_VIEW_KEY.componentIndex)
            viewports.remove(viewId)?.dispose()
    }

    actual override fun createTexture(data: TextureData): Triple<Int, Int, Int> {
        val colorConverter = data.colorConverter

        val texture = if (colorConverter != INT_FUNCTION_IDENTITY)
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
        if (data.textureIndex !in textures)
            throw IllegalStateException("Texture with id: ${data.textureIndex} not loaded" )

        val texture = textures[data.textureIndex]
        val sprite = TextureRegion(
            texture,
            data.textureBounds.x,
            data.textureBounds.y,
            data.textureBounds.width,
            data.textureBounds.height
        )

        sprite.flip(data.hFlip, !data.vFlip)
        return sprites.add(sprite)
    }

    actual override fun disposeSprite(spriteId: Int) {
        sprites.remove(spriteId)
    }

    actual override fun createShader(data: ShaderData): Int {
        var vertexShader = data.vertexShaderProgram
        var fragmentShader = data.fragmentShaderProgram

        if (vertexShader == NO_PROGRAM) {
            // try to load from resource
            vertexShader = try {
                Gdx.files.internal(data.vertexShaderResourceName).readString()
            } catch (e: Exception) {
                if (data.vertexShaderResourceName != NO_NAME)
                    System.err.println("Failed to load vertex shader from resource: ${data.vertexShaderResourceName}")
                // DEBUG  println("--> Use default vertex shader")
                DEFAULT_VERTEX_SHADER
            }
        }

        if (fragmentShader == NO_PROGRAM) {
            fragmentShader = try {
                Gdx.files.internal(data.fragmentShaderResourceName).readString()
            } catch (e: Exception) {
                if (data.fragmentShaderResourceName != NO_NAME)
                    System.err.println("Failed to load fragment shader from resource: ${data.fragmentShaderResourceName}")
                // DEBUG  println("--> Use default fragment shader")
                DEFAULT_FRAGMENT_SHADER
            }
        }

        val shaderProgram = ShaderProgram(vertexShader, fragmentShader)
        if (shaderProgram.isCompiled)
            // DEBUG  println("--> Shader Compiled: ${shaderProgram.log}")
        else {
            System.err.println("Shader program failed to compile:")
            System.err.println("Vertex Shader:")
            System.err.println(vertexShader)
            System.err.println("Fragment Shader:")
            System.err.println(fragmentShader)
            throw RuntimeException("com.inari.firefly.core.api.ShaderData failed to compile:" + shaderProgram.log)
        }

        val shader = Shader(shaderProgram, data.shaderUpdate)
        return shaders + shader
    }

    actual override fun disposeShader(shaderId: Int) {
        shaders.remove(shaderId)?.dispose()
    }

    actual override fun clearView(view: ViewData) {
        viewports[view.index]?.activate(spriteBatch, shapeRenderer, view, true)
    }

    actual override fun startViewportRendering(view: ViewData, clear: Boolean) {
        if (activeBackBufferId >= 0)
            throw IllegalStateException("Back-buffer rendering is on")
        if (activeViewportId >= 0)
            throw IllegalStateException("Viewport rendering is on")

        val activeViewport = viewports[view.index]
        activeViewport?.activate(spriteBatch, shapeRenderer, view, clear)
        activeViewportId = view.index
        spriteBatch.begin()
    }

    actual override fun applyViewportOffset(x: Float, y: Float) {
        if (activeViewportId < 0) return
        viewports[activeViewportId]?.offset(x, y)
    }

    actual override fun setActiveShader(shaderId: Int) {
        if (shaderId != activeShaderId) {
            spriteBatch.flush()
            if (shaderId < 0) {
                spriteBatch.shader = defaultShaderProgram
                activeShaderId = -1
            } else {
                val shaderData = shaders[shaderId]
                if (shaderData != null) {
                    shaderData.activate()
                    activeShaderId = shaderId
                }
            }
        }
        if (shaderId != activeShapeShaderId)
            if (shaderId < 0)
                (shapeRenderer.renderer as ImmediateModeRenderer20).shader = defaultShaderProgram
            else {
                val shader = shaders[shaderId]
                if (shader != null) {
                    shader.activate()
                    (shapeRenderer.renderer as ImmediateModeRenderer20).shader = shader.program
                }
            }
        activeShapeShaderId = shaderId
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
        blendMode: BlendMode) {

        val texture = textures[textureId] ?: return
        setColorAndBlendMode(tintColor, blendMode)
        spriteBatch.draw(
            texture,
            posX, posY,
            posX, posY,
            texture.width.toFloat(), texture.height.toFloat(),
            scaleX, scaleY, rotation,
            0, 0, texture.width, texture.height,
            flipX, flipY
        )
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, xOffset: Float, yOffset: Float) {
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        val sprite = sprites[renderableSprite.spriteIndex]
        spriteBatch.draw(sprite, xOffset, yOffset)
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData) {
        val sprite = sprites[renderableSprite.spriteIndex] ?: return
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        spriteBatch.draw(
            sprite,
            transform.position.x,
            transform.position.y,
            transform.pivot.x,
            transform.pivot.y,
            sprite.regionWidth.toFloat(),
            sprite.regionHeight.toFloat(),
            transform.scale.x,
            transform.scale.y,
            transform.rotation
        )
    }

    actual override fun renderSprite(renderableSprite: SpriteRenderable, transform: TransformData, xOffset: Float, yOffset: Float) {
        val sprite = sprites[renderableSprite.spriteIndex] ?: return
        setColorAndBlendMode(renderableSprite.tintColor, renderableSprite.blendMode)
        spriteBatch.draw(
            sprite,
            transform.position.x + xOffset,
            transform.position.y + yOffset,
            transform.pivot.x,
            transform.pivot.y,
            sprite.regionWidth.toFloat(),
            sprite.regionHeight.toFloat(),
            transform.scale.v0,
            transform.scale.v1,
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
            transformMatrix.translate(transform.position.x + transform.pivot.x, transform.position.y + transform.pivot.y, 0f)
            transformMatrix.scale(transform.scale.v0, transform.scale.v1, 0f)
            transformMatrix.translate(-(transform.position.x + transform.pivot.x), -(transform.position.y + transform.pivot.y), 0f)
            spriteBatch.transformMatrix = transformMatrix
        }

        if (transform.hasRotation) {
            transformMatrix.translate(transform.position.x + transform.pivot.x, transform.position.y + transform.pivot.y, 0f)
            transformMatrix.rotate(0f, 0f, 1f, transform.rotation)
            transformMatrix.translate(-(transform.position.x + transform.pivot.x), -(transform.position.y + transform.pivot.y), 0f)
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
            shapeRenderer.scale(transform.scale.v0, transform.scale.v1, 0f)
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

    actual override fun endViewportRendering(view: ViewData) {
        if (activeViewportId < 0)
            throw IllegalStateException("No viewport rendering active")
        if (view.index != activeViewportId)
            throw IllegalStateException("Other viewport rendering id active")

        // flush the batch and deactivate the viewport
        spriteBatch.flush()
        if (!view.isBase )
            viewports[activeViewportId]?.deactivate()
        spriteBatch.end()
        activeViewportId = -1
    }

    actual override fun flush(virtualViews: DynArrayRO<ViewData>) {
        if (!virtualViews.isEmpty) {
            var i = 0
            while (i < virtualViews.capacity) {
                val virtualView = virtualViews[i++] ?: continue
                val viewport = viewports[virtualView.index] ?: continue

                // if we have a render pipeline applied to this view, render it first
                if (virtualView.isRenderTarget)
                    renderPipeline(virtualView)

                // then activate the base view port and render the virtual viewport to it
                baseViewport?.activate(spriteBatch, shapeRenderer, baseView!!, true)

                spriteBatch.begin()

                setColorAndBlendMode(virtualView.tintColor, virtualView.blendMode)
                setActiveShader(virtualView.shaderIndex)

                spriteBatch.draw(
                    viewport.fboTexture,
                    virtualView.bounds.x.toFloat(), virtualView.bounds.y.toFloat(),
                    virtualView.bounds.width.toFloat(), virtualView.bounds.height.toFloat()
                )

                spriteBatch.end()
            }
        }

        spriteBatch.flush()
        activeBlend = BlendMode.NONE
        setActiveShader(-1)
    }

    private fun renderPipeline(targetViewPort: ViewData) {
        if (targetViewPort.renderTargetOf1 >= 0)
            renderPipeline(targetViewPort, viewports[targetViewPort.renderTargetOf1])
        if (targetViewPort.renderTargetOf2 >= 0)
            renderPipeline(targetViewPort, viewports[targetViewPort.renderTargetOf2])
        if (targetViewPort.renderTargetOf3 >= 0)
            renderPipeline(targetViewPort, viewports[targetViewPort.renderTargetOf3])
    }

    private fun renderPipeline(targetViewPort: ViewData, source: ViewportFBO?) {
        if (source != null) {
            val viewport = viewports[targetViewPort.index] ?: return
            // recursively go up to the root render source
            if (source.viewData.isRenderTarget)
                renderPipeline(source.viewData)
            // then render the source to the target
            viewport.activate(spriteBatch, shapeRenderer, targetViewPort, targetViewPort.clearBeforeStartRendering)
            spriteBatch.begin()

            setColorAndBlendMode(source.viewData.tintColor, source.viewData.blendMode)
            setActiveShader(source.viewData.shaderIndex)

            spriteBatch.draw(
                source.fboTexture,
                source.viewData.bounds.x.toFloat(), source.viewData.bounds.y.toFloat(),
                source.viewData.bounds.width.toFloat(), source.viewData.bounds.height.toFloat()
            )

            spriteBatch.flush()
            viewport.deactivate()
            spriteBatch.end()
        }
    }

    actual override fun getTexturePixels(textureId: Int): ByteArray {
        val texture = this.textures[textureId] ?: return ByteArray(0)
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()

        val byteBuf = if (pixmap.format != Pixmap.Format.RGBA8888) {
            // convert to RGBA8888
            val newPixmap = Pixmap(pixmap.width, pixmap.height, Pixmap.Format.RGBA8888)
            newPixmap.drawPixmap(pixmap, 0, 0)
            newPixmap.pixels
        } else
            pixmap.pixels

        pixmap.pixels

        val byteArray = ByteArray(byteBuf.capacity())
        byteBuf.get(byteArray)
        return byteArray
    }

    actual override fun setTexturePixels(textureId: Int, region: Vector4i, pixels: ByteArray) {
        val texture = this.textures[textureId] ?: return
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()

        val bi = pixels.iterator()
        for (y in region.y until region.y + region.height) {
            for (x in region.x until region.x + region.width) {
                pixmap.drawPixel(x, y,
                    ((bi.nextByte().toInt() and 0xff) shl 24) or
                    ((bi.nextByte().toInt() and 0xff) shl 16) or
                    ((bi.nextByte().toInt() and 0xff) shl 8) or
                    (bi.nextByte().toInt() and 0xff)
                )
            }
        }

        // create new texture from
        this.textures[textureId] = Texture(pixmap)
        // update all active sprites that uses the old texture.
        this.sprites.forEach {
            if (it.texture == texture)
                it.texture = this.textures[textureId]
        }
    }

    actual override fun getScreenshotPixels(region: Vector4i): ByteArray {
        val flippedY = screenHeight - region.height + region.y
        val size = region.width * region.height * 3
        val screenContents = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN)
        GL11.glReadPixels(region.x, flippedY, region.width, region.height, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, screenContents)
        val array = ByteArray(size)
        val inverseArray = ByteArray(size)
        screenContents.get(array)

        var y = 0
        while (y < region.height) {
            System.arraycopy(
                array,
                GeomUtils.getFlatArrayIndex(0, region.height - y - 1, region.width * 3),
                inverseArray,
                GeomUtils.getFlatArrayIndex(0, y, region.width * 3),
                region.width * 3
            )
            y++
        }
        return inverseArray
    }

    private fun createBaseViewport(viewPort: ViewData): ViewportFBO =
        ViewportFBO(viewPort, null, null)

    private fun createVirtualViewport(viewPort: ViewData): ViewportFBO {
        val frameBuffer = FrameBuffer(
            Pixmap.Format.RGBA8888,
            (viewPort.bounds.width * viewPort.fboScale).toInt(),
            (viewPort.bounds.height * viewPort.fboScale).toInt(),
            false
        )
        val textureRegion = TextureRegion(frameBuffer.colorBufferTexture)
        textureRegion.flip(false, false)

        return ViewportFBO(viewPort, frameBuffer, textureRegion)
    }


    private fun getLibGDXTextureWrap(glConst: Int): TextureWrap =
        TextureWrap.values().firstOrNull { it.glEnum == glConst }
            ?: TextureWrap.Repeat

    private fun getLibGDXTextureFilter(glConst: Int): TextureFilter =
        TextureFilter.values().firstOrNull { it.glEnum == glConst }
            ?: TextureFilter.Linear

    private fun setColorAndBlendMode(renderColor: Vector4f, blendMode: BlendMode) {
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

    private fun getShapeColor(rgbColor: Vector4f, color: Color) =
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

    private class Shader (
        val program: ShaderProgram,
        val shaderInit: (ShaderUpdate) -> Unit
    ) : ShaderUpdate {

        private var texNum = 1

        fun activate() {
            spriteBatch.shader = program
            shaderInit(this)
            Gdx.graphics.gL20.glActiveTexture(GL20.GL_TEXTURE0)
        }
        fun dispose() {
            spriteBatch.shader = null
        }

        override fun setUniformFloat(bindingName: String, value: Float) =
            program.setUniformf(bindingName, value)
        override fun setUniformVec2(bindingName: String, value: Vector2f) =
            program.setUniformf(bindingName, value.x, value.y)
        override fun setUniformVec3(bindingName: String, value: Vector3f) =
            program.setUniformf(bindingName, value.v0, value.v1, value.v2)
        override fun setUniformColorVec4(bindingName: String, value: Vector4f) =
            program.setUniformf(bindingName, value.r, value.g, value.b, value.a)
        override fun bindTexture(bindingName: String, value: Int) {
            program.setUniformi(bindingName, texNum)
            textures[value]?.bind(texNum)
            texNum++
        }
        override fun bindViewTexture(bindingName: String, value: Int) {
            viewports[value]?.bindToShader(program, bindingName, texNum++)
        }
    }

    private class ViewportFBO constructor(
        var viewData: ViewData,
        val frameBuffer: FrameBuffer?,
        val fboTexture: TextureRegion?) {

        val camera: OrthographicCamera = OrthographicCamera(
            viewData.bounds.width.toFloat(),
            viewData.bounds.height.toFloat()
        )

        fun activate(
            spriteBatch: PolygonSpriteBatch,
            shapeRenderer: ShapeRenderer,
            view: ViewData? = null,
            clear: Boolean = true
        ) {
            if (view != null)
                viewData = view

            val worldPosition = viewData.worldPosition
            val zoom = viewData.zoom
            val clearColor = viewData.clearColor
            val bounds = viewData.bounds

            camera.setToOrtho(true, bounds.width * zoom, bounds.height * zoom)
            camera.position.x = camera.position.x + worldPosition.x
            camera.position.y = camera.position.y + worldPosition.y
            camera.update()
            spriteBatch.projectionMatrix = camera.combined
            shapeRenderer.projectionMatrix = camera.combined

            frameBuffer?.begin() // this also binds the FBO

            if (clear) {
                Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
            }
        }

        fun offset(x: Float, y: Float) {
            camera.position.x = camera.position.x + x
            camera.position.y = camera.position.y + y
            camera.update()
            spriteBatch.projectionMatrix = camera.combined
            shapeRenderer.projectionMatrix = camera.combined
        }

        fun bindToShader(program: ShaderProgram, binding: String, num: Int) {
            program.setUniformi(binding, num)
            fboTexture?.texture?.bind(num)
        }

        fun deactivate() = frameBuffer?.end()
        fun dispose() = frameBuffer?.dispose()
    }
}