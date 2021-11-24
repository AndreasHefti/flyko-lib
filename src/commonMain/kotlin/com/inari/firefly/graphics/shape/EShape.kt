package com.inari.firefly.graphics.shape

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.component.PropertyRefResolver
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.effect.ShaderAsset
import com.inari.firefly.graphics.tile.ETile
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

class EShape private constructor(): EntityComponent(EShape::class.simpleName!!) {

    @JvmField val data = ShapeData()

    var shapeType: ShapeType
        get() = data.type
        set(value) { data.type = value }
    var vertices: FloatArray
        get() = data.vertices
        set(value) { data.vertices = value }
    var color: Vector4f
        get() = data.color1
        set(value) = data.color1(value)
    var gradientColor1: Vector4f
        get() = data.color2!!
        set(value) {data.color2 = value}
    var gradientColor2: Vector4f
        get() = data.color3!!
        set(value) {data.color3 = value}
    var gradientColor3: Vector4f
        get() = data.color4!!
        set(value) {data.color4 = value}
    var segments: Int
        get() = data.segments
        set(value) { data.segments = value }
    var fill: Boolean
        get() = data.fill
        set(value) { data.fill = value }
    var blend: BlendMode
        get() = data.blend
        set(value) { data.blend = value }

    override fun reset() {
        data.reset()
    }

    override fun toString(): String {
        return "EShape(subType=$data.subType, " +
            "vertices=${data.vertices}, " +
            "color1=${data.color1}, " +
            "color2=${data.color2}, " +
            "color3=${data.color3}, " +
            "color4=${data.color4}, " +
            "segments=${data.segments}, " +
            "fill=${data.fill}, " +
            "blend=${data.blend}, "
    }

    object Property {
        val TINT_ALPHA: PropertyRefResolver<Float> = { FFContext[Entity, it][EShape].color::a }
        val TINT_COLOR: PropertyRefResolver<Vector4f> = { FFContext[Entity, it][EShape]::color }
    }

    override fun componentType(): ComponentType<EShape> = Companion
    companion object : EntityComponentType<EShape>(EShape::class) {
        override fun createEmpty() = EShape()
    }
}