package com.inari.firefly.graphics.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.firefly.graphics.sprite.SpriteSet
import com.inari.util.FloatPropertyAccessor
import com.inari.util.IntPropertyAccessor
import com.inari.util.VOID_INT_PROPERTY_ACCESSOR
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField

class ETile private constructor(): EntityComponent(ETile) {

    @JvmField val renderData = SpriteRenderable()

    var spriteIndex: BindingIndex
        get() = renderData.spriteIndex
        set(value) { renderData.spriteIndex = value }
    val tintColor
        get() = renderData.tintColor
    var blendMode
        get() = renderData.blendMode
        set(value) { renderData.blendMode = value }
    @JvmField val position: Vector2i = Vector2i()
    @JvmField val tileGridRef = CReference(TileGrid)
    @JvmField val spriteRef = CReference(Sprite)
    @JvmField val spriteSetRef = CReference(SpriteSet)

    var spriteRefPropertyAccessor: IntPropertyAccessor = VOID_INT_PROPERTY_ACCESSOR
        private set
        get() {
            if (field == VOID_INT_PROPERTY_ACCESSOR)
                field = object : IntPropertyAccessor {
                    override fun invoke(value: Int) { spriteIndex = value }
                }
            return field
        }

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

    object PropertyAccessor {
        private inline fun getTintColor(index: EntityIndex) = ETile[index].tintColor
        private inline fun getTintColorRed(index: EntityIndex) = getTintColor(index).v0PropertyAccessor
        private inline fun getTintColorGreen(index: EntityIndex) = getTintColor(index).v1PropertyAccessor
        private inline fun getTintColorBlue(index: EntityIndex) = getTintColor(index).v2PropertyAccessor
        private inline fun getTintColorAlpha(index: EntityIndex) = getTintColor(index).v3PropertyAccessor
        private inline fun getSpriteIndex(index: EntityIndex) = ETile[index].spriteRefPropertyAccessor
        @JvmField val TINT_COLOR_RED: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorRed
        @JvmField val TINT_COLOR_GREEN: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorGreen
        @JvmField val TINT_COLOR_BLUE: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorBlue
        @JvmField val TINT_COLOR_ALPHA: (EntityIndex) -> FloatPropertyAccessor = this::getTintColorAlpha
        @JvmField val SPRITE: (EntityIndex) -> IntPropertyAccessor = this::getSpriteIndex
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<ETile>("ETile") {
        override fun allocateArray() = DynArray.of<ETile>()
        override fun create() = ETile()
    }
}