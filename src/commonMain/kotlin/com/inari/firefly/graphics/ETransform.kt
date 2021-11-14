package com.inari.firefly.graphics

import com.inari.firefly.FFContext
import com.inari.firefly.ZERO_FLOAT
import com.inari.firefly.ZERO_INT
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.TransformData
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.firefly.physics.animation.PropertyRefResolver
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class ETransform private constructor() : EntityComponent(ETransform::class.simpleName!!), ViewLayerAware {

    @JvmField internal var viewRef = 0
    @JvmField internal var layerRef = 0
    @JvmField internal val data = TransformData()

    val view = ComponentRefResolver(View) { index-> viewRef = index }
    val layer = ComponentRefResolver(Layer) { index-> layerRef = index }
    var position: Vector2f
        get() = data.position
        set(value) = data.position(value)
    var pivot: Vector2f
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

    fun move(dx: Int = ZERO_INT, dy: Int = ZERO_INT) {
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

    object Property {
        val POSITION_X: PropertyRefResolver<Float> = { FFContext[Entity, it][ETransform].data.position::x }
        val POSITION_Y: PropertyRefResolver<Float> = { FFContext[Entity, it][ETransform].data.position::y }
        val POSITION: PropertyRefResolver<Vector2f> = { FFContext[Entity, it][ETransform]::position }
        val SCALE_X: PropertyRefResolver<Float> = { FFContext[Entity, it][ETransform].data.scale::v0 }
        val SCALE_Y: PropertyRefResolver<Float> = { FFContext[Entity, it][ETransform].data.scale::v1 }
        val SCALE: PropertyRefResolver<Vector2f> = { FFContext[Entity, it][ETransform]::scale }
        val ROTATION: PropertyRefResolver<Float> = { FFContext[Entity, it][ETransform].data::rotation }
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<ETransform>(ETransform::class) {
        override fun createEmpty() = ETransform()
    }
}