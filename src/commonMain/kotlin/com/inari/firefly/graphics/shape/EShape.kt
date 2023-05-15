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

class EShape private constructor() : EntityComponent(EShape) {

    @JvmField val data = ShapeData()

    var type: ShapeType
        get() = data.type
        set(value) { data.type = value }
    var vertices: FloatArray
        get() = data.vertices
        set(value) { data.vertices = value }
    var segments: Int
        get() = data.segments
        set(value) { data.segments = value }
    val color1: Vector4f
        get() = data.color1
    var color2: Vector4f?
        get() = data.color2
        set(value) { data.color2 = value }
    var color3: Vector4f?
        get() = data.color3
        set(value) { data.color3 = value }
    var color4: Vector4f?
        get() = data.color4
        set(value) { data.color4 = value }
    var blend: BlendMode
        get() = data.blend
        set(value) { data.blend = value }
    var fill: Boolean
        get() = data.fill
        set(value) { data.fill = value }

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
        fun getColorRed(index: EntityIndex) = getColor(index).v0PropertyAccessor
        fun getColorGreen(index: EntityIndex) = getColor(index).v1PropertyAccessor
        fun getColorBlue(index: EntityIndex) = getColor(index).v2PropertyAccessor
        fun getColorAlpha(index: EntityIndex) = getColor(index).v3PropertyAccessor
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