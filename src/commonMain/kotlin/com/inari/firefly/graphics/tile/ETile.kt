package com.inari.firefly.graphics.tile

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.TILE_ASPECT_GROUP
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.entity.property.IntPropertyAccessor
import com.inari.firefly.entity.property.VirtualPropertyRef
import com.inari.firefly.graphics.effect.ShaderAsset
import com.inari.util.aspect.Aspects
import com.inari.util.geom.Position
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

class ETile private constructor () : EntityComponent(ETile::class.simpleName!!) {

    @JvmField internal val spriteRenderable = SpriteRenderable()

    @Suppress("SetterBackingFieldAssignment")
    var aspects: Aspects = TILE_ASPECT_GROUP.createAspects()
        set(value) {
            field.clear()
            field + value
        }
    val sprite = AssetInstanceRefResolver(
        { instanceId -> spriteRenderable.spriteId = instanceId },
        { spriteRenderable.spriteId })
    var blend: BlendMode
        get() = spriteRenderable.blendMode
        set(value) { spriteRenderable.blendMode = value }
    var tint: MutableColor
        get() = spriteRenderable.tintColor
        set(value) { spriteRenderable.tintColor(value) }
    val position: Position = Position()

    override fun reset() {
        spriteRenderable.reset()
        position.x = 0
        position.y = 0
    }

    private val accessorSpriteRef: IntPropertyAccessor = object : IntPropertyAccessor {
        override fun set(value: Int) {spriteRenderable.spriteId = value}
        override fun get(): Int = spriteRenderable.spriteId
    }
    private val accessorTintRed: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {spriteRenderable.tintColor.r_mutable = value}
        override fun get(): Float = spriteRenderable.tintColor.r
    }
    private val accessorTintGreen: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {spriteRenderable.tintColor.g_mutable = value}
        override fun get(): Float = spriteRenderable.tintColor.g
    }
    private val accessorTintBlue: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {spriteRenderable.tintColor.b_mutable = value}
        override fun get(): Float = spriteRenderable.tintColor.b
    }
    private val accessorTintAlpha: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {spriteRenderable.tintColor.a_mutable = value}
        override fun get(): Float = spriteRenderable.tintColor.a
    }

    enum class Property(
        override val propertyName: String,
        override val type: KClass<*>
    ) : VirtualPropertyRef {
        SPRITE_REFERENCE("spriteRef", Int::class) {
            override fun accessor(entity: Entity): IntPropertyAccessor {
                return entity[ETile].accessorSpriteRef
            }
        },
        TINT_RED("tintRed", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETile].accessorTintRed
            }
        },
        TINT_GREEN("tintGreen", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETile].accessorTintGreen
            }
        },
        TINT_BLUE("tintBlue", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETile].accessorTintBlue
            }
        },
        TINT_ALPHA("tintAlpha", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETile].accessorTintAlpha
            }
        }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ETile>(ETile::class) {
        override fun createEmpty() = ETile()
    }
}