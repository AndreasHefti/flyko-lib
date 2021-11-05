package com.inari.firefly.graphics.sprite

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.entity.property.IntPropertyAccessor
import com.inari.firefly.entity.property.VirtualPropertyRef
import com.inari.firefly.graphics.effect.ShaderEffectAsset
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField
import kotlin.reflect.KClass


class ESprite private constructor() : EntityComponent(ESprite::class.simpleName!!) {

    @JvmField internal val spriteRenderable = SpriteRenderable()

    val sprite = AssetInstanceRefResolver(
        { index -> spriteRenderable.spriteId = index },
        { spriteRenderable.spriteId })
    val effect = AssetInstanceRefResolver(
        { index -> spriteRenderable.effectInstanceRef = FFContext[ShaderEffectAsset, index].instanceId },
        { spriteRenderable.effectInstanceRef })
    var blend: BlendMode
        get() = spriteRenderable.blendMode
        set(value) { spriteRenderable.blendMode = value }
    var tint: MutableColor
        get() = spriteRenderable.tintColor
        set(value) { spriteRenderable.tintColor(value) }

    override fun reset() {
        spriteRenderable.reset()
    }

    override fun toString(): String {
        return "ESprite(spriteRef=${spriteRenderable.spriteId}, " +
            "effectInstanceRef=${spriteRenderable.effectInstanceRef}, " +
            "blend=${spriteRenderable.blendMode}, " +
            "tint=${spriteRenderable.tintColor}, "
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
                return entity[ESprite].accessorSpriteRef
            }
        },
        TINT_RED("tintRed", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ESprite].accessorTintRed
            }
        },
        TINT_GREEN("tintGreen", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ESprite].accessorTintGreen
            }
        },
        TINT_BLUE("tintBlue", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ESprite].accessorTintBlue
            }
        },
        TINT_ALPHA("tintAlpha", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ESprite].accessorTintAlpha
            }
        }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ESprite>(ESprite::class) {
        override fun createEmpty() = ESprite()
    }
}