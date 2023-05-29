package com.inari.firefly.core.api

import com.inari.firefly.core.ComponentKey
import com.inari.util.*
import com.inari.util.collection.AttributesRO
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector3f
import com.inari.util.geom.Vector4f
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

const val NULL_COMPONENT_INDEX = -1
const val NULL_BINDING_INDEX = -1

typealias ComponentIndex = Int
typealias EntityIndex = Int
typealias BindingIndex = Int

enum class ActionResult {
    SUCCESS,
    RUNNING,
    FAILED
}

interface Action {
    operator fun invoke(index: EntityIndex = -1): ActionResult
}
typealias ActionCallback = (index: EntityIndex, ActionResult) -> Unit
@JvmField val VOID_ACTION: Action = object : Action {
    override fun invoke(index: EntityIndex) = ActionResult.SUCCESS
}
@JvmField val VOID_ACTION_CALLBACK: ActionCallback = VOID_CONSUMER_2

interface Condition {
    operator fun invoke(index: EntityIndex = -1): Boolean
}
@JvmField val TRUE_CONDITION = object : Condition {
    override fun invoke(index: EntityIndex) = true
}
@JvmField val FALSE_CONDITION = object : Condition {
    override fun invoke(index: EntityIndex) = false
}

interface NormalOperation {
    operator fun invoke(ci1: ComponentIndex, ci2: ComponentIndex, ci3: ComponentIndex): Float
}
@JvmField val ZERO_OP = object : NormalOperation {
    override fun invoke(ci1: ComponentIndex, ci2: ComponentIndex, ci3: ComponentIndex) = ZERO_FLOAT
}
@JvmField val ONE_OP = object : NormalOperation {
    override fun invoke(ci1: ComponentIndex, ci2: ComponentIndex, ci3: ComponentIndex) = 1f
}

typealias SimpleTask = () -> Unit
typealias TaskOperation = (ComponentKey, AttributesRO, TaskCallback) -> Unit
typealias TaskCallback = (ComponentKey, AttributesRO, ActionResult) -> Unit
@JvmField val VOID_TASK_OPERATION: TaskOperation = VOID_CONSUMER_3
@JvmField val VOID_TASK_CALLBACK: TaskCallback = VOID_CONSUMER_3

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
    val shaderIndex: BindingIndex
    val zoom: Float
    val fboScale: Float
    val index: BindingIndex
    val renderTargetOf1: BindingIndex
    val renderTargetOf2: BindingIndex
    val renderTargetOf3: BindingIndex
    val isRenderTarget: Boolean
        get() = renderTargetOf1 >= 0 || renderTargetOf2 >= 0 || renderTargetOf3 >= 0
    val renderToBase: Boolean
    val isBase: Boolean
}

open class TransformData {
    @JvmField val position = Vector2f()
    @JvmField val pivot = Vector2f()
    @JvmField val scale = Vector2f(1f, 1f)
    @JvmField var rotation = ZERO_FLOAT
    val hasRotation: Boolean get() = rotation != ZERO_FLOAT
    val hasScale: Boolean get() = scale.v0 != 1.0f || scale.v1 != 1.0f
}

class ShaderData {
    @JvmField var vertexShaderResourceName = NO_NAME
    @JvmField var vertexShaderProgram = NO_PROGRAM
    @JvmField var fragmentShaderResourceName = NO_NAME
    @JvmField var fragmentShaderProgram = NO_PROGRAM
    @JvmField var shaderUpdate: (ShaderUpdate) -> Unit = VOID_CONSUMER_1
}

interface ShaderUpdate {
    fun setUniformFloat(bindingName: String, value: Float)
    fun setUniformVec2(bindingName: String, value: Vector2f)
    fun setUniformVec3(bindingName: String, value: Vector3f)
    fun setUniformColorVec4(bindingName: String, value:Vector4f)
    fun bindTexture(bindingName: String, value: Int)
    fun bindViewTexture(bindingName: String, value: Int)
}

class TextureData {
    @JvmField var resourceName = NO_NAME
    @JvmField var isMipmap = false
    @JvmField var wrapS = -1
    @JvmField var wrapT = -1
    @JvmField var minFilter = -1
    @JvmField var magFilter = -1
    @JvmField var colorConverter: (Int) -> Int = INT_FUNCTION_IDENTITY
}

class SpriteData {
    @JvmField var textureIndex: BindingIndex = -1
    @JvmField var textureBounds = Vector4i(ZERO_INT, ZERO_INT, ZERO_INT, ZERO_INT)
    @JvmField var hFlip = false
    @JvmField var vFlip = false
}

class SpriteRenderable {
    @JvmField var spriteIndex: BindingIndex = NULL_BINDING_INDEX
    @JvmField val tintColor = Vector4f(1f, 1f, 1f, 1f)
    @JvmField var blendMode = BlendMode.NORMAL_ALPHA
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

class ShapeData {
    @JvmField var type = ShapeType.POINT
    @JvmField var vertices = floatArrayOf()
    @JvmField var segments = 0
    @JvmField val color1 = Vector4f()
    @JvmField var color2: Vector4f? = null
    @JvmField var color3: Vector4f? = null
    @JvmField var color4: Vector4f? = null
    @JvmField var blend = BlendMode.NONE
    @JvmField var fill = true
}