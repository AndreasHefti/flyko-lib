package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_BINDING_INDEX
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.util.FloatPropertyAccessor
import com.inari.util.IntPropertyAccessor
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class ESprite private constructor() : EntityComponent(ESprite), SpriteRenderable {

    override var spriteIndex = NULL_BINDING_INDEX
    override val tintColor = Vector4f(1f, 1f, 1f, 1f)
    override var blendMode = BlendMode.NONE

    @JvmField val spriteRef = CReference(Sprite)
    @JvmField val spriteSetRef = CReference(SpriteSet)

    override fun activate() {
        if (spriteRef.exists)
            spriteIndex = Asset.resolveAssetIndex(spriteRef.targetKey)
        if (spriteSetRef.exists)
            spriteIndex = Asset.resolveAssetIndex(spriteSetRef.targetKey)
    }

    fun getSpriteIndexPropertyAccessor(): IntPropertyAccessor = object : IntPropertyAccessor {
        override fun invoke(value: Int) { spriteIndex = value }
        override fun invoke(): Int = spriteIndex
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

    object PropertyAccessor {
        fun getInstance(index: ComponentIndex) = ComponentSystem[Entity, index][ESprite]
        fun getTintColor(index: ComponentIndex) = getInstance(index).tintColor
        fun getTintColorRed(index: ComponentIndex) = getTintColor(index).getV0PropertyAccessor()
        fun getTintColorGreen(index: ComponentIndex) = getTintColor(index).getV1PropertyAccessor()
        fun getTintColorBlue(index: ComponentIndex) = getTintColor(index).getV2PropertyAccessor()
        fun getTintColorAlpha(index: ComponentIndex) = getTintColor(index).getV3PropertyAccessor()
        fun getSpriteIndex(index: ComponentIndex) = getInstance(index).getSpriteIndexPropertyAccessor()
        @JvmField val SPRITE_INDEX = this::getSpriteIndex
        @JvmField val TINT_COLOR_RED: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorRed
        @JvmField val TINT_COLOR_GREEN: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorGreen
        @JvmField val TINT_COLOR_BLUE: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorBlue
        @JvmField val TINT_COLOR_ALPHA: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorAlpha
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ESprite>("ESprite") {
        override fun create() = ESprite()
    }
}