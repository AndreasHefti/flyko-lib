package com.inari.firefly.core.api

import com.inari.util.ZERO_FLOAT
import com.inari.util.geom.*
import kotlin.jvm.JvmField

enum class BlendMode constructor(val source: Int, val dest: Int) {
    /** No blending. Disables blending  */
    NONE(-1, -1),
    /** Normal alpha blending. GL11: GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA  */
    NORMAL_ALPHA(GLBlendMode.GL_SRC_ALPHA, GLBlendMode.GL_ONE_MINUS_SRC_ALPHA),
    /** Additive blending ( without alpha ). GL11: GL_ONE, GL_ONE )  */
    ADDITIVE(GLBlendMode.GL_ONE, GLBlendMode.GL_ONE),
    /** Additive blending ( with alpha ). GL11: GL_SRC_ALPHA, GL_ONE  */
    ADDITIVE_ALPHA(GLBlendMode.GL_SRC_ALPHA, GLBlendMode.GL_ONE),
    /** Multiplied blending. GL11: GL_DST_COLOR, GL_ZERO  */
    MULT(GLBlendMode.GL_DST_COLOR, GLBlendMode.GL_DST_COLOR),
    /** Clears the destination. GL11: GL_ZERO, GL_ZERO  */
    CLEAR(GLBlendMode.GL_ZERO, GLBlendMode.GL_ZERO),
    /** The source overlaps the destination. GL11: GL_ONE, GL_ZERO  */
    SRC(GLBlendMode.GL_ONE, GLBlendMode.GL_ZERO),
    /** Only the destination. GL11: GL_ZERO, GL_ONE  */
    DEST(GLBlendMode.GL_ZERO, GLBlendMode.GL_ZERO),
    SRC_OVER_DEST(GLBlendMode.GL_ONE, GLBlendMode.GL_ONE_MINUS_SRC_ALPHA),
    DEST_OVER_SRC(GLBlendMode.GL_ONE_MINUS_DST_ALPHA, GLBlendMode.GL_ONE),
    SRC_IN_DEST(GLBlendMode.GL_DST_ALPHA, GLBlendMode.GL_ZERO),
    DEST_IN_SRC(GLBlendMode.GL_ONE, GLBlendMode.GL_SRC_ALPHA),
    SRC_OUT_DEST(GLBlendMode.GL_ONE_MINUS_DST_ALPHA, GLBlendMode.GL_ZERO),
    DEST_OUT_SRC(GLBlendMode.GL_ZERO, GLBlendMode.GL_ONE_MINUS_SRC_ALPHA),
    SRC_ATOP_DEST(GLBlendMode.GL_DST_ALPHA, GLBlendMode.GL_ONE_MINUS_SRC_ALPHA),
    DEST_ATOP_SRC(GLBlendMode.GL_ONE_MINUS_DST_ALPHA, GLBlendMode.GL_DST_ALPHA),
    SRC_XOR_DEST(GLBlendMode.GL_ONE_MINUS_DST_ALPHA, GLBlendMode.GL_ONE_MINUS_SRC_ALPHA),
}

object GLBlendMode {
    const val GL_ZERO = 0x0
    const val GL_ONE = 0x1
    const val GL_SRC_COLOR = 0x300
    const val GL_ONE_MINUS_SRC_COLOR = 0x301
    const val GL_SRC_ALPHA = 0x302
    const val GL_ONE_MINUS_SRC_ALPHA = 0x303
    const val GL_DST_ALPHA = 0x304
    const val GL_ONE_MINUS_DST_ALPHA = 0x305
    const val GL_DST_COLOR = 0x306
    const val GL_ONE_MINUS_DST_COLOR = 0x307
    const val GL_SRC_ALPHA_SATURATE = 0x308
    const val GL_CONSTANT_COLOR = 0x8001
    const val GL_ONE_MINUS_CONSTANT_COLOR = 0x8002
    const val GL_CONSTANT_ALPHA = 0x8003
    const val GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004
}

interface ViewData {
    val bounds: Vector4i
    val worldPosition: Vector2f
    val clearColor: Vector4f
    val tintColor: Vector4f
    val blendMode: BlendMode
    val shaderIndex: Int
    val zoom: Float
    val fboScale: Float
    val index: Int
    val isBase: Boolean
}

interface TransformData {
    val position: Vector2f
    val pivot: Vector2f
    val scale: Vector2f
    val rotation: Float
    val hasRotation: Boolean get() = rotation != ZERO_FLOAT
    val hasScale: Boolean get() = scale.v0 != 1.0f || scale.v1 != 1.0f
}

class TransformDataImpl() : TransformData {
    override val position = Vector2f()
    override val pivot = Vector2f()
    override val scale = Vector2f()
    override var rotation = 1.0f
}

interface ShaderData {
    val vertexShaderResourceName: String
    val vertexShaderProgram: String
    val fragmentShaderResourceName: String
    val fragmentShaderProgram: String
    val shaderInit: ShaderInit
}

typealias ShaderInit = (ShaderInitAdapter) -> Unit
interface ShaderInitAdapter {
    fun setUniformFloat(bindingName: String, value: Float)
    fun setUniformVec2(bindingName: String, position: Vector2f)
    fun setUniformVec2(bindingName: String, position: Vector2i)
    fun setUniformVec3(bindingName: String, v: Vector3f)
    fun setUniformColorVec4(bindingName: String, color: Vector4f)
    fun bindTexture(bindingName: String, textureId: Int)
    fun bindBackBuffer(bindingName: String, backBufferId: Int)
}

interface TextureData {
    val resourceName: String
    val isMipmap: Boolean
    val wrapS: Int
    val wrapT: Int
    val minFilter: Int
    val magFilter: Int
    val colorConverter: (Int) -> Int
}

interface SpriteData {
    val textureIndex: Int
    val region: Vector4i
    val isHorizontalFlip: Boolean
    val isVerticalFlip: Boolean
}

interface SpriteRenderable {
    val spriteIndex: Int
    val tintColor: Vector4f
    val blendMode: BlendMode
}

class SpriteRenderableImpl : SpriteRenderable {
    override var spriteIndex = -1
    override val tintColor = Vector4f(1f, 1f, 1f, 1f)
    override var blendMode = BlendMode.NORMAL_ALPHA
}

enum class ShapeType {
    POINT,
    LINE,
    POLY_LINE,
    POLYGON,
    RECTANGLE,
    CIRCLE,
    ARC,
    CURVE,
    TRIANGLE
}

interface ShapeData {
    val type: ShapeType
    val vertices: FloatArray
    val segments: Int
    val color1: Vector4f
    val color2: Vector4f?
    val color3: Vector4f?
    val color4: Vector4f?
    val blend: BlendMode
    val fill: Boolean
}

interface FrameBufferData {
    val bounds: Vector4i
    val clearColor: Vector4f
    val tintColor: Vector4f
    val blendMode: BlendMode
    val shaderRef: Int
    val zoom: Float
    val fboScale: Float
}