package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.TransformData
import com.inari.util.FloatPropertyAccessor
import com.inari.util.IntPropertyAccessor
import com.inari.util.ZERO_FLOAT
import com.inari.util.ZERO_INT
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class ETransform private constructor() : EntityComponent(ETransform), TransformData {

    val view = CReference(View)
    val layer = CReference(Layer)
    val viewIndex: Int
        get() = view.targetKey.instanceId
    val layerIndex: Int
        get() = if (layer.exists) layer.targetKey.instanceId else 0
    override val position: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    override val pivot: Vector2f = Vector2f(ZERO_FLOAT, ZERO_FLOAT)
    override val scale: Vector2f = Vector2f(1.0f, 1.0f)
    override var rotation: Float = ZERO_FLOAT

    fun getRotationPropertyAccessor(): FloatPropertyAccessor = object : FloatPropertyAccessor {
        override fun invoke(value: Float) { rotation = value }
        override fun invoke(): Float = rotation
    }

    fun move(dx: Float = ZERO_FLOAT, dy: Float = ZERO_FLOAT) {
        position.x += dx
        position.y += dy
    }

    fun move(dx: Int = ZERO_INT, dy: Int = ZERO_INT) {
        position.x += dx
        position.y += dy
    }

    override fun reset() {
        view.reset()
        layer.reset()
        position.x = ZERO_FLOAT
        position.y = ZERO_FLOAT
        pivot.x = ZERO_FLOAT
        pivot.y = ZERO_FLOAT
        scale.v0 = 1.0f
        scale.v1 = 1.0f
        rotation = ZERO_FLOAT
    }

    object PropertyAccessor {
        fun getInstance(index: Int) = ComponentSystem[Entity, index][ETransform]
        fun getPos(index: Int) = getInstance(index).position
        fun getScale(index: Int) = getInstance(index).scale
        fun getPosXAccessor(index: Int) = getPos(index).getV0PropertyAccessor()
        fun getPosYAccessor(index: Int) = getPos(index).getV1PropertyAccessor()
        fun getScaleXAccessor(index: Int) = getScale(index).getV0PropertyAccessor()
        fun getScaleYAccessor(index: Int) = getScale(index).getV1PropertyAccessor()
        fun getRotationAccessor(index: Int) = getInstance(index).getRotationPropertyAccessor()
        @JvmField val POSITION_X: (Int) -> FloatPropertyAccessor = this::getPosXAccessor
        @JvmField val POSITION_Y: (Int) -> FloatPropertyAccessor = this::getPosYAccessor
        @JvmField val SCALE_X: (Int) -> FloatPropertyAccessor = this::getScaleXAccessor
        @JvmField val SCALE_Y: (Int) -> FloatPropertyAccessor = this::getScaleYAccessor
        @JvmField val ROTATION: (Int) -> FloatPropertyAccessor = this::getRotationAccessor
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ETransform>("ETransform") {
        override fun create() = ETransform()
    }
}