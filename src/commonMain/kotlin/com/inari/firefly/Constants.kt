package com.inari.firefly

import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.entity.Entity
import com.inari.util.*
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.geom.*
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0

const val ZERO_INT = 0
const val ZERO_FLOAT = 0.0f
const val SYSTEM_FONT_ASSET = "SYSTEM_FONT_ASSET"
const val SYSTEM_FONT = "SYSTEM_FONT"

const val UNDERLINE = "_"
const val COLON = ":"
const val EMPTY_STRING = ""
const val COMMA = ","

object Color {
    val BLACK = ImmutableVector4f(0.0f, 0.0f, 0.0f, 1.0f)
    val WHITE = ImmutableVector4f(1.0f, 1.0f, 1.0f, 1.0f)
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

const val NO_NAME: String = "[[NO_NAME]]"
const val NO_STATE: String = "[[NO_STATE]]"
const val NO_PROGRAM: String = "[[NO_PROGRAM]]"
const val BASE_VIEW: String = "[[BASE_VIEW]]"

@JvmField val EMPTY_FLOAT_ARRAY: FloatArray = FloatArray(0)

@JvmField val NO_NAMED = object : Named { override val name = NO_NAME }
@JvmField val NO_COMP_ID: CompId = CompId(-1, object : ComponentType<Component> {
    override val aspectIndex: Int get() = throw UnsupportedOperationException()
    override val aspectName: String get() = throw UnsupportedOperationException()
    override val aspectType: AspectType get() = throw UnsupportedOperationException()
    override val typeClass: KClass<Component> get() = throw UnsupportedOperationException()
    override val subTypeClass: KClass<out Component>
        get() = typeClass
})
@JvmField val NO_INDEXED = object : Indexed {
    override val index: Int = -1
    override val indexedTypeName: String = NO_NAME
}


@JvmField val VOID_INT_CONSUMER: IntConsumer = { _ -> }
@JvmField val INT_FUNCTION_IDENTITY: IntFunction = { i -> i }
@JvmField val INT_FUNCTION_NULL: IntFunction = { _ -> 0 }
@JvmField val NULL_INT_FUNCTION: IntFunction = { _ -> throw IllegalStateException("NULL_INT_FUNCTION") }
@JvmField val NULL_INT_CONSUMER: IntConsumer = { _ -> throw IllegalStateException("NULL_INT_CONSUMER") }
@JvmField val EMPTY_INT_CONSUMER: IntConsumer = { _ -> }
@JvmField val NULL_CONSUMER: Consumer<Any> = { _ -> throw IllegalStateException("NULL_CONSUMER") }
@JvmField val NULL_CALL: Call = { throw IllegalStateException("NULL_CALL called") }
@JvmField val DO_NOTHING = {}
@JvmField val FALSE_SUPPLIER = { false }
@JvmField val TRUE_SUPPLIER = { true }
@JvmField val FALSE_PREDICATE: Predicate<Any> = { false }
@JvmField val TRUE_PREDICATE: Predicate<Any> = { true }
@JvmField val VOID_COMP_ID_CONSUMER: Consumer<CompId> = { _ -> }
@JvmField val VOID_CONSUMER: Consumer<Any> = { _ -> }

@JvmField val INFINITE_SCHEDULER: FFTimer.Scheduler = object : FFTimer.Scheduler {
    override val resolution: Float = 60f
    override fun needsUpdate(): Boolean = true
}
@JvmField val TRUE_INT_PREDICATE: IntPredicate =  { true }
@JvmField val FALSE_INT_PREDICATE: IntPredicate = { false }

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

// normalized vecrtors

@JvmField val VEC_NF_NORTH = ImmutableVector2f(0f, 1f)
@JvmField val VEC_NF_SOUTH = ImmutableVector2f(0f, -1f)
@JvmField val VEC_NF_WEST = ImmutableVector2f(-1f, 0f)
@JvmField val VEC_NF_EAST = ImmutableVector2f(1f, 0f)

@JvmField val VEC_NI_NORTH = ImmutableVector2i(0, 1)
@JvmField val VEC_NI_SOUTH = ImmutableVector2i(0, -1)
@JvmField val VEC_NI_WEST = ImmutableVector2i(-1, 0)
@JvmField val VEC_NI_EAST = ImmutableVector2i(1, 0)


// Following some pre defined aspect groups

@JvmField val ENTITY_CONTROL_ASPECT_GROUP = IndexedAspectType("ENTITY_CONTROL_ASPECTS")
@JvmField val UNDEFINED_CONTROL: Aspect = ENTITY_CONTROL_ASPECT_GROUP.createAspect("UNDEFINED_CONTROL")

@JvmField val MATERIAL_ASPECT_GROUP = IndexedAspectType("MATERIAL_ASPECT_GROUP")
@JvmField val UNDEFINED_MATERIAL: Aspect = MATERIAL_ASPECT_GROUP.createAspect("UNDEFINED_MATERIAL")

@JvmField val TILE_ASPECT_GROUP = IndexedAspectType("TILE_ASPECT_GROUP")
@JvmField val UNDEFINED_TILE_ASPECT = TILE_ASPECT_GROUP.createAspect("UNDEFINED_TILE_ASPECT")

@JvmField val CONTACT_TYPE_ASPECT_GROUP = IndexedAspectType("CONTACT_TYPE_ASPECT_GROUP")
@JvmField val UNDEFINED_CONTACT_TYPE: Aspect = CONTACT_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_CONTACT_TYPE")

@JvmField val PROJECTILE_TYPE_ASPECT_GROUP = IndexedAspectType("PROJECTILE_TYPE_ASPECT_GROUP")
@JvmField val UNDEFINED_PROJECTILE_TYPE = PROJECTILE_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_PROJECTILE_TYPE")

@JvmField val ACTOR_CATEGORY_ASPECT_GROUP = IndexedAspectType("ACTOR_CATEGORY_ASPECT_GROUP")
@JvmField val UNDEFINED_ACTOR_CATEGORY = ACTOR_CATEGORY_ASPECT_GROUP.createAspect("UNDEFINED_ACTOR_CATEGORY")

@JvmField val ACTOR_TYPE_ASPECT_GROUP = IndexedAspectType("ACTOR_TYPE_ASPECT_GROUP")
@JvmField val UNDEFINED_ACTOR_TYPE = ACTOR_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_ACTOR_TYPE")

@JvmField val MOVEMENT_ASPECT_GROUP = IndexedAspectType("MOVEMENT_ASPECT_GROUP")
@JvmField val UNDEFINED_MOVEMENT = MOVEMENT_ASPECT_GROUP.createAspect("UNDEFINED_MOVEMEN")
enum class MovementAspect(private val aspect: Aspect) : Aspect {
    ON_GROUND(MOVEMENT_ASPECT_GROUP.createAspect("ON_GROUND")),
    BLOCK_WEST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_WEST")),
    BLOCK_EAST(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_EAST")),
    BLOCK_NORTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_NORTH")),
    BLOCK_SOUTH(MOVEMENT_ASPECT_GROUP.createAspect("BLOCK_SOUTH")),
    ;
    override val aspectIndex: Int get() = aspect.aspectIndex
    override val aspectName: String get() = aspect.aspectName
    override val aspectType: AspectType get() = aspect.aspectType
}





