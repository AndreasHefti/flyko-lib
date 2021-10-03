package com.inari.firefly.graphics

import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.TransformData
import com.inari.util.geom.PositionF
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.property.FloatPropertyAccessor
import com.inari.firefly.entity.property.VirtualPropertyRef
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField
import kotlin.reflect.KClass

class ETransform private constructor() : EntityComponent(ETransform::class.simpleName!!), ViewLayerAware {

    @JvmField internal var viewRef = 0
    @JvmField internal var layerRef = 0
    @JvmField internal val data = TransformData()

    val view = ComponentRefResolver(View) { index-> viewRef = index }
    val layer = ComponentRefResolver(Layer) { index-> layerRef = index }
    var position: PositionF
        get() = data.position
        set(value) = data.position(value)
    var pivot: PositionF
        get() = data.pivot
        set(value) = data.pivot(value)
    var scale: Vector2f
        get() = data.scale
        set(value) { data.scale(value) }
    var rotation: Float
        get() = data.rotation
        set(value) { data.rotation = value }

    override val viewIndex: Int
        get() = viewRef
    override val layerIndex: Int
        get() = layerRef

    fun move(dx: Float = ZERO_FLOAT, dy: Float = ZERO_FLOAT) {
        data.position.x += dx
        data.position.y += dy
    }

    fun move(dx: Int = 0, dy: Int = 0) {
        data.position.x += dx
        data.position.y += dy
    }

    override fun reset() {
        viewRef = 0
        layerRef = 0
        data.reset()
    }

    override fun toString(): String {
        return "ETransform(viewRef=$viewRef, " +
            "layerRef=$layerRef, " +
            "position=${data.position}, " +
            "pivot=${data.pivot}, " +
            "scale=${data.scale}, " +
            "rot=${data.rotation})"
    }

    private val accessorPosX: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.position.x = value}
        override fun get(): Float = data.position.x
    }
    private val accessorPosY: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.position.y = value}
        override fun get(): Float = data.position.y
    }
    private val accessorPivotX: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.pivot.x = value}
        override fun get(): Float = data.pivot.x
    }
    private val accessorPivotY: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.pivot.y = value}
        override fun get(): Float = data.pivot.y
    }
    private val accessorScaleX: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.scale.dx = value}
        override fun get(): Float = data.scale.dx
    }
    private val accessorScaleY: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.scale.dy = value}
        override fun get(): Float = data.scale.dy
    }
    private val accessorRotation: FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun set(value: Float) {data.rotation = value}
        override fun get(): Float = data.rotation
    }

    enum class Property(
        override val propertyName: String,
        override val type: KClass<*>
    ) : VirtualPropertyRef {
        POSITION_X("position.x", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorPosX
            }
        },
        POSITION_Y("position.y", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorPosY
            }
        },
        PIVOT_X("pivot.x", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorPivotX
            }
        },
        PIVOT_Y("pivot.y", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorPivotY
            }
        },
        SCALE_X("scale.x", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorScaleX
            }
        },
        SCALE_Y("scale.y", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorScaleY
            }
        },
        ROTATION("rotation", Float::class) {
            override fun accessor(entity: Entity): FloatPropertyAccessor {
                return entity[ETransform].accessorRotation
            }
        }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ETransform>(ETransform::class) {
        override fun createEmpty() = ETransform()
    }
}