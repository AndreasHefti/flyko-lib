package com.inari.firefly.graphics.sprite

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.effect.ShaderAsset
import com.inari.firefly.physics.animation.PropertyRefResolver
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

class ESprite private constructor() : EntityComponent(ESprite::class.simpleName!!) {

    @JvmField internal val spriteRenderable = SpriteRenderable()

    val sprite = AssetInstanceRefResolver(
        { instanceId -> spriteRenderable.spriteId = instanceId },
        { spriteRenderable.spriteId })
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
            "blend=${spriteRenderable.blendMode}, " +
            "tint=${spriteRenderable.tintColor}, "
    }

    object Property {
        val SPRITE_REFERENCE: PropertyRefResolver<Int> = { FFContext[Entity, it][ESprite].spriteRenderable::spriteId }
        val TINT_RED: PropertyRefResolver<Float> = { FFContext[Entity, it][ESprite].spriteRenderable.tintColor::r_mutable }
        val TINT_GREEN: PropertyRefResolver<Float> = { FFContext[Entity, it][ESprite].spriteRenderable.tintColor::g_mutable }
        val TINT_BLUE: PropertyRefResolver<Float> = { FFContext[Entity, it][ESprite].spriteRenderable.tintColor::b_mutable }
        val TINT_ALPHA: PropertyRefResolver<Float> = { FFContext[Entity, it][ESprite].spriteRenderable.tintColor::a_mutable }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ESprite>(ESprite::class) {
        override fun createEmpty() = ESprite()
    }
}