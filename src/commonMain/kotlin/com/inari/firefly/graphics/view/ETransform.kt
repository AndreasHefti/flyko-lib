package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.TransformData
import com.inari.util.FloatPropertyAccessor
import com.inari.util.VOID_FLOAT_PROPERTY_ACCESSOR
import com.inari.util.ZERO_FLOAT
import com.inari.util.ZERO_INT
import com.inari.util.collection.DynArray
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class ETransform private constructor() : EntityComponent(ETransform), ViewLayerAware {

    @JvmField val renderData = TransformData()

    @JvmField val viewRef = CReference(View)
    @JvmField val layerRef = CReference(Layer)
    val position: Vector2f
        get() = renderData.position
    val pivot: Vector2f
        get() = renderData.pivot
    val scale: Vector2f
        get() = renderData.scale
    var rotation: Float
        get() = renderData.rotation
        set(value) { renderData.rotation = value }

    override val viewIndex: Int
        get() = viewRef.targetKey.componentIndex
    override val layerIndex: Int
        get() = if (layerRef.exists) layerRef.targetKey.componentIndex else 0

    var rotationPropertyAccessor: FloatPropertyAccessor = VOID_FLOAT_PROPERTY_ACCESSOR
        private set
        get() {
            if (field == VOID_FLOAT_PROPERTY_ACCESSOR)
                field = object : FloatPropertyAccessor {
                    override fun invoke(value: Float) { rotation = value }
                }
            return field
        }

    override fun activate() {
        super.activate()
        // if view is not defined yet, set the base view for default
        if (!viewRef.defined)
            viewRef(View.BASE_VIEW_KEY)
        else if (viewRef.exists)
            View.activate(viewRef.targetKey)
        if (layerRef.exists)
            Layer.activate(layerRef.targetKey)
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
        viewRef.reset()
        layerRef.reset()
        position.x = ZERO_FLOAT
        position.y = ZERO_FLOAT
        pivot.x = ZERO_FLOAT
        pivot.y = ZERO_FLOAT
        scale.v0 = 1.0f
        scale.v1 = 1.0f
        rotation = ZERO_FLOAT
    }

    object PropertyAccessor {
        private inline fun getPos(index: Int) = ETransform[index].position
        private inline fun getScale(index: Int) = ETransform[index].scale
        private inline fun getPosXAccessor(index: Int) = getPos(index).v0PropertyAccessor
        private inline fun getPosYAccessor(index: Int) = getPos(index).v1PropertyAccessor
        private inline fun getScaleXAccessor(index: Int) = getScale(index).v0PropertyAccessor
        private inline fun getScaleYAccessor(index: Int) = getScale(index).v1PropertyAccessor
        private inline fun getRotationAccessor(index: Int) = ETransform[index].rotationPropertyAccessor
        @JvmField val POSITION_X: (Int) -> FloatPropertyAccessor = this::getPosXAccessor
        @JvmField val POSITION_Y: (Int) -> FloatPropertyAccessor = this::getPosYAccessor
        @JvmField val SCALE_X: (Int) -> FloatPropertyAccessor = this::getScaleXAccessor
        @JvmField val SCALE_Y: (Int) -> FloatPropertyAccessor = this::getScaleYAccessor
        @JvmField val ROTATION: (Int) -> FloatPropertyAccessor = this::getRotationAccessor
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<ETransform>("ETransform") {
        override fun allocateArray() = DynArray.of<ETransform>()
        override fun create() = ETransform()
    }
}