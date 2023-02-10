package com.inari.firefly.graphics.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.SpriteSet
import com.inari.util.FloatPropertyAccessor
import com.inari.util.IntPropertyAccessor
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class ETile private constructor(): EntityComponent(ETile), SpriteRenderable {

    override var spriteIndex = NULL_BINDING_INDEX
    override val tintColor = Vector4f(1f, 1f, 1f, 1f)
    override var blendMode = BlendMode.NONE
    @JvmField val position: Vector2i = Vector2i()

    @JvmField val tileGridRef = CReference(TileGrid)
    @JvmField val spriteRef = CReference(Sprite)
    @JvmField val spriteSetRef = CReference(SpriteSet)

    override fun activate() {
        if (tileGridRef.exists)
            TileGrid.load(tileGridRef.targetKey)
        if (spriteRef.exists)
            spriteIndex = Asset.resolveAssetIndex(spriteRef.targetKey)
        if (spriteSetRef.exists)
            spriteIndex = Asset.resolveAssetIndex(spriteSetRef.targetKey)
    }

    override fun deactivate() {
        spriteIndex = NULL_BINDING_INDEX
    }

    override fun reset() {
        spriteIndex = NULL_BINDING_INDEX
        spriteRef.reset()
        spriteSetRef.reset()
        tintColor(1f, 1f, 1f, 1f)
        blendMode = BlendMode.NONE
    }

    fun getSpriteIndexPropertyAccessor(): IntPropertyAccessor = object : IntPropertyAccessor {
        override fun invoke(value: Int) { spriteIndex = value }
        override fun invoke(): Int = spriteIndex
    }

    object PropertyAccessor {
        fun getInstance(index: EntityIndex) = ComponentSystem[Entity, index][ETile]
        fun getTintColor(index: EntityIndex) = getInstance(index).tintColor
        fun getTintColorRed(index: EntityIndex) = getTintColor(index).getV0PropertyAccessor()
        fun getTintColorGreen(index: EntityIndex) = getTintColor(index).getV1PropertyAccessor()
        fun getTintColorBlue(index: EntityIndex) = getTintColor(index).getV2PropertyAccessor()
        fun getTintColorAlpha(index: EntityIndex) = getTintColor(index).getV3PropertyAccessor()
        fun getSpriteIndex(index: EntityIndex) = getInstance(index).getSpriteIndexPropertyAccessor()
        @JvmField val TINT_COLOR_RED: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorRed
        @JvmField val TINT_COLOR_GREEN: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorGreen
        @JvmField val TINT_COLOR_BLUE: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorBlue
        @JvmField val TINT_COLOR_ALPHA: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorAlpha
        @JvmField val SPRITE: (EntityIndex) -> IntPropertyAccessor = this::getSpriteIndex
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ETile>("ETile") {
        override fun create() = ETile()
    }
}