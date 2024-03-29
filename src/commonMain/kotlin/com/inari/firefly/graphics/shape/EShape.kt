package com.inari.firefly.graphics.shape

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentSystem
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.util.FloatPropertyAccessor
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

class EShape private constructor() : EntityComponent(EShape) {

    @JvmField val renderData = ShapeData()

    var type: ShapeType
        get() = renderData.type
        set(value) { renderData.type = value }
    var vertices: FloatArray
        get() = renderData.vertices
        set(value) { renderData.vertices = value }
    var segments: Int
        get() = renderData.segments
        set(value) { renderData.segments = value }
    val color1: Vector4f
        get() = renderData.color1
    var color2: Vector4f?
        get() = renderData.color2
        set(value) { renderData.color2 = value }
    var color3: Vector4f?
        get() = renderData.color3
        set(value) { renderData.color3 = value }
    var color4: Vector4f?
        get() = renderData.color4
        set(value) { renderData.color4 = value }
    var blend: BlendMode
        get() = renderData.blend
        set(value) { renderData.blend = value }
    var fill: Boolean
        get() = renderData.fill
        set(value) { renderData.fill = value }

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
        private inline fun getColorRed(index: EntityIndex) = EShape[index].color1.v0PropertyAccessor
        private inline fun getColorGreen(index: EntityIndex) = EShape[index].color1.v1PropertyAccessor
        private inline fun getColorBlue(index: EntityIndex) = EShape[index].color1.v2PropertyAccessor
        private inline fun getColorAlpha(index: EntityIndex) = EShape[index].color1.v3PropertyAccessor
        @JvmField val COLOR_RED: (EntityIndex) -> FloatPropertyAccessor = this::getColorRed
        @JvmField val COLOR_GREEN: (EntityIndex) -> FloatPropertyAccessor = this::getColorGreen
        @JvmField val COLOR_BLUE: (EntityIndex) -> FloatPropertyAccessor = this::getColorBlue
        @JvmField val COLOR_ALPHA: (EntityIndex) -> FloatPropertyAccessor = this::getColorAlpha
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<EShape>("EShape") {
        override fun allocateArray() = DynArray.of<EShape>()
        override fun create() = EShape()
    }
}