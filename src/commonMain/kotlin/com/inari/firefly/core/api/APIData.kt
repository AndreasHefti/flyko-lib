package com.inari.firefly.core.api

import com.inari.util.VOID_CONSUMER_3
import com.inari.util.ZERO_FLOAT
import com.inari.util.collection.Dictionary
import com.inari.util.geom.*
import kotlin.jvm.JvmField

const val NULL_COMPONENT_INDEX = -1
const val NULL_BINDING_INDEX = -1

//typealias ComponentIndex = Int
//typealias EntityIndex = Int
//typealias BindingIndex = Int

enum class OperationResult {
    SUCCESS,
    RUNNING,
    FAILED
}
typealias Action = (Int) -> OperationResult
typealias ActionCallback = (Int, OperationResult) -> Unit
operator fun ActionCallback.invoke() = this(NULL_COMPONENT_INDEX, OperationResult.SUCCESS)
operator fun ActionCallback.invoke(result: OperationResult) = this(NULL_COMPONENT_INDEX, result)
@JvmField val SUCCESS_ACTION: Action = { _ -> OperationResult.SUCCESS }
@JvmField val FAILED_ACTION: Action = { _ -> OperationResult.FAILED }
@JvmField val RUNNING_ACTION: Action = { _ -> OperationResult.RUNNING }
@JvmField val NO_ACTION: Action = FAILED_ACTION


typealias NormalOperation = (Int, Int, Int) -> Float
@JvmField val ZERO_OP: NormalOperation = { _, _, _ -> 0f }
@JvmField val ONE_OP : NormalOperation = { _, _, _ -> 1f }

typealias SimpleTask = (Int) -> Unit
typealias TaskOperation = (Int, Dictionary, TaskCallback) -> Unit
typealias TaskCallback = (Int, Dictionary, OperationResult) -> Unit
@JvmField val VOID_TASK_OPERATION: TaskOperation = VOID_CONSUMER_3
@JvmField val VOID_TASK_CALLBACK: TaskCallback = VOID_CONSUMER_3

typealias MoveCallback = (Int, Float, ButtonType) -> Unit
@JvmField val VOID_MOVE_CALLBACK: MoveCallback = VOID_CONSUMER_3

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
    val clearBeforeStartRendering: Boolean
    val tintColor: Vector4f
    val blendMode: BlendMode
    val shaderIndex: Int
    val zoom: Float
    val fboScale: Float
    val index: Int
    val renderTargetOf1: Int
    val renderTargetOf2: Int
    val renderTargetOf3: Int
    val isRenderTarget: Boolean
        get() = renderTargetOf1 >= 0 || renderTargetOf2 >= 0 || renderTargetOf3 >= 0
    val renderToBase: Boolean
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
    val shaderUpdate: (ShaderUpdate) -> Unit
}

interface ShaderUpdate {
    fun setUniformFloat(bindingName: String, value: Float)
    fun setUniformVec2(bindingName: String, value: Vector2f)
    fun setUniformVec3(bindingName: String, value: Vector3f)
    fun setUniformColorVec4(bindingName: String, value:Vector4f)
    fun bindTexture(bindingName: String, value: Int)
    fun bindViewTexture(bindingName: String, value: Int)
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
    val textureBounds: Vector4i
    val hFlip: Boolean
    val vFlip: Boolean
}

interface SpriteRenderable {
    val spriteIndex: Int
    val tintColor: Vector4f
    val blendMode: BlendMode
}

class SpriteRenderableImpl : SpriteRenderable {
    override var spriteIndex: Int = NULL_BINDING_INDEX
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