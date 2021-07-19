package com.inari.firefly.graphics.shape

import com.inari.firefly.BlendMode
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.api.ShapeData
import com.inari.firefly.core.api.ShapeType
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.entity.property.VirtualPropertyRef
import com.inari.util.graphics.MutableColor
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
    var color: MutableColor
        get() = data.color1
        set(value) = data.color1(value)
    var gradientColor1: MutableColor
        get() = data.color2!!
        set(value) {data.color2 = value}
    var gradientColor2: MutableColor
        get() = data.color3!!
        set(value) {data.color3 = value}
    var gradientColor3: MutableColor
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
    val shader = AssetInstanceRefResolver(
        { index -> data.shaderRef = index },
        { data.shaderRef })

    override fun reset() {
        data.reset()
    }

    override fun toString(): String {
        return "EShape(subType=$data.subType, " +
            "vertices=${data.vertices.contentToString()}, " +
            "color1=$data.color1, " +
            "color2=$data.color2, " +
            "color3=$data.color3, " +
            "color4=$data.color4, " +
            "segments=$data.segments, " +
            "fill=$data.fill, " +
            "blend=$data.blend, " +
            "shaderRef=$data.shaderRef, " +
            "ShaderAsset=$shader)"
    }

    private val accessorColorRed: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.color1.r_mutable = value}
        override fun get(): Float = data.color1.r
    }
    private val accessorColorGreen: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.color1.g_mutable = value}
        override fun get(): Float = data.color1.g
    }
    private val accessorColorBlue: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.color1.b_mutable = value}
        override fun get(): Float = data.color1.b
    }
    private val accessorColorAlpha: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.color1.a_mutable = value}
        override fun get(): Float = data.color1.a
    }

    enum class Property(
        override val propertyName: String,
        override val type: KClass<*>
    ) : VirtualPropertyRef {
        COLOR_RED("colorRed", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[EShape].accessorColorRed
            }
        },
        COLOR_GREEN("colorGreen", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[EShape].accessorColorGreen
            }
        },
        COLOR_BLUE("colorBlue", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[EShape].accessorColorBlue
            }
        },
        COLOR_ALPHA("colorAlpha", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[EShape].accessorColorAlpha
            }
        }
    }

    override fun componentType(): ComponentType<EShape> = Companion
    companion object : EntityComponentType<EShape>(EShape::class) {
        override fun createEmpty() = EShape()
    }
}