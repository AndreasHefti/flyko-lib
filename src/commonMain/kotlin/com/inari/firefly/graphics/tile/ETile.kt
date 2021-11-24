package com.inari.firefly.graphics.tile

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.TILE_ASPECT_GROUP
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.core.component.PropertyRefResolver
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

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
    var tint: Vector4f
        get() = spriteRenderable.tintColor
        set(value) { spriteRenderable.tintColor(value) }
    val position: Vector2i = Vector2i()

    override fun reset() {
        spriteRenderable.reset()
        position.x = 0
        position.y = 0
    }

    object Property {
        val SPRITE_REFERENCE: PropertyRefResolver<Int> = { FFContext[Entity, it][ETile].spriteRenderable::spriteId }
        val TINT_ALPHA: PropertyRefResolver<Float> = { FFContext[Entity, it][ETile].spriteRenderable.tintColor::a }
        val TINT_COLOR: PropertyRefResolver<Vector4f> = { FFContext[Entity, it][ETile].spriteRenderable::tintColor }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ETile>(ETile::class) {
        override fun createEmpty() = ETile()
    }
}