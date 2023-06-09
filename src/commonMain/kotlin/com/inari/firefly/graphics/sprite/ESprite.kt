package com.inari.firefly.graphics.sprite

import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.util.FloatPropertyAccessor
import com.inari.util.IntPropertyAccessor
import com.inari.util.VOID_INT_PROPERTY_ACCESSOR
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class ESprite private constructor() : EntityComponent(ESprite) {

    @JvmField val renderData = SpriteRenderable()

    var spriteIndex: BindingIndex
        get() = renderData.spriteIndex
        set(value) { renderData.spriteIndex = value }
    val tintColor
        get() = renderData.tintColor
    var blendMode
        get() = renderData.blendMode
        set(value) { renderData.blendMode = value }
    @JvmField val spriteRef = CReference(Sprite)
    @JvmField val spriteSetRef = CReference(SpriteSet)
    var spriteRefPropertyAccessor: IntPropertyAccessor = VOID_INT_PROPERTY_ACCESSOR
        private set
        get() {
            if (field == VOID_INT_PROPERTY_ACCESSOR)
                field = object : IntPropertyAccessor {
                    override fun invoke(value: Int) { renderData.spriteIndex = value }
                }
            return field
        }

    override fun activate() {
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

    object PropertyAccessor {
        private inline fun getTintColor(index: ComponentIndex) = ESprite[index].renderData.tintColor
        private inline fun getTintColorRed(index: ComponentIndex) = getTintColor(index).v0PropertyAccessor
        private inline fun getTintColorGreen(index: ComponentIndex) = getTintColor(index).v1PropertyAccessor
        private inline fun getTintColorBlue(index: ComponentIndex) = getTintColor(index).v2PropertyAccessor
        private inline fun getTintColorAlpha(index: ComponentIndex) = getTintColor(index).v3PropertyAccessor
        private inline fun getSpriteIndex(index: ComponentIndex) = ESprite[index].spriteRefPropertyAccessor
        @JvmField val SPRITE_INDEX = this::getSpriteIndex
        @JvmField val TINT_COLOR_RED: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorRed
        @JvmField val TINT_COLOR_GREEN: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorGreen
        @JvmField val TINT_COLOR_BLUE: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorBlue
        @JvmField val TINT_COLOR_ALPHA: (ComponentIndex) -> FloatPropertyAccessor = this::getTintColorAlpha
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<ESprite>("ESprite") {
        override fun allocateArray() = DynArray.of<ESprite>()
        override fun create() = ESprite()
    }
}