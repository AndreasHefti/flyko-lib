package com.inari.firefly.graphics.tile

import com.inari.firefly.core.*
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.util.FloatPropertyAccessor
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class ETile private constructor(): EntityComponent(ETile), SpriteRenderable {


    override var spriteIndex = -1
        internal set
    override val tintColor = Vector4f(1f, 1f, 1f, 1f)
    override var blendMode = BlendMode.NONE
    @JvmField val spriteRef = CReference(Sprite)
    @JvmField val position: Vector2i = Vector2i()
    @JvmField val tileGridRef = CReference(TileGrid)

    override fun activate() {
        spriteIndex = Asset.resolveAssetIndex(spriteRef.targetKey)
    }

    override fun deactivate() {
        spriteIndex = -1
    }

    override fun reset() {
        spriteIndex = -1
        spriteRef.reset()
        tintColor(1f, 1f, 1f, 1f)
        blendMode = BlendMode.NONE
    }

    object PropertyAccessor {
        fun getInstance(index: Int) = ComponentSystem[Entity, index][ETile]
        fun getTintColor(index: Int) = getInstance(index).tintColor
        fun getTintColorRed(index: Int) = getTintColor(index).getV0PropertyAccessor()
        fun getTintColorGreen(index: Int) = getTintColor(index).getV1PropertyAccessor()
        fun getTintColorBlue(index: Int) = getTintColor(index).getV2PropertyAccessor()
        fun getTintColorAlpha(index: Int) = getTintColor(index).getV3PropertyAccessor()
        @JvmField val TINT_COLOR_RED: (Int) -> FloatPropertyAccessor = this::getTintColorRed
        @JvmField val TINT_COLOR_GREEN: (Int) -> FloatPropertyAccessor = this::getTintColorGreen
        @JvmField val TINT_COLOR_BLUE: (Int) -> FloatPropertyAccessor = this::getTintColorBlue
        @JvmField val TINT_COLOR_ALPHA: (Int) -> FloatPropertyAccessor = this::getTintColorAlpha
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ETile>("ETile") {
        override fun create() = ETile()
    }
}