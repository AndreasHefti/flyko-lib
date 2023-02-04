package com.inari.firefly.graphics.shape

import com.inari.firefly.core.ComponentSystem
import com.inari.firefly.core.Entity
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.util.FloatPropertyAccessor
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class EShape private constructor() : EntityComponent(EShape), ShapeData {

    override var type: ShapeType = ShapeType.POINT
    override var vertices: FloatArray = floatArrayOf()
    override var segments: Int = -1
    val color: Vector4f
        get() = color1
    override val color1 = Vector4f(1f, 1f, 1f, 1f)
    override var color2 = null
    override var color3 = null
    override var color4 = null
    override var blend = BlendMode.NONE
    override var fill = false

    override fun reset() {
        type = ShapeType.POINT
        vertices = floatArrayOf()
        segments = -1
        color1(1f, 1f, 1f, 1f)
        color2 = null
        color3 = null
        color4 = null
        blend = BlendMode.NONE
        fill = false
    }

    object PropertyAccessor {
        fun getInstance(index: EntityIndex) = ComponentSystem[Entity, index][EShape]
        fun getColor(index: EntityIndex) = getInstance(index).color1
        fun getColorRed(index: EntityIndex) = getColor(index).getV0PropertyAccessor()
        fun getColorGreen(index: EntityIndex) = getColor(index).getV1PropertyAccessor()
        fun getColorBlue(index: EntityIndex) = getColor(index).getV2PropertyAccessor()
        fun getColorAlpha(index: EntityIndex) = getColor(index).getV3PropertyAccessor()
        @JvmField val COLOR_RED: (EntityIndex) -> FloatPropertyAccessor = this::getColorRed
        @JvmField val COLOR_GREEN: (EntityIndex) -> FloatPropertyAccessor = this::getColorGreen
        @JvmField val COLOR_BLUE: (EntityIndex) -> FloatPropertyAccessor = this::getColorBlue
        @JvmField val COLOR_ALPHA: (EntityIndex) -> FloatPropertyAccessor = this::getColorAlpha
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EShape>("EShape") {
        override fun create() = EShape()
    }
}