package com.inari.firefly.graphics.rendering

import com.inari.util.geom.PositionF
import com.inari.firefly.TRUE_PREDICATE
import com.inari.firefly.core.api.TransformData
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.entity.EChild
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.Consumer
import com.inari.util.Predicate
import com.inari.util.collection.DynArray
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField
import kotlin.math.floor

abstract class Renderer protected constructor(
    private val acceptance: Predicate<Entity> = TRUE_PREDICATE,
    private val sort: Consumer<DynArray<Entity>>? = null
) : SystemComponent(Renderer::class.simpleName!!) {

    @JvmField
    protected val transformCollector = ExactTransformDataCollector()

    private val entities: DynArray<DynArray<DynArray<Entity>>> = DynArray.of()

    fun accept(entity: Entity): Boolean {
        return if (acceptance(entity)) {
            forceGet(entity[ETransform])?.apply {
                add(entity)
                sort?.invoke(this)
            }
            true
        } else
            false
    }

    fun dispose(entity: Entity) {
        if (acceptance(entity))
            this[entity[ETransform]]?.remove(entity)
    }

    internal fun clearView(viewIndex: Int) =
        entities.remove(viewIndex)

    internal fun clearLayer(viewIndex: Int, layerIndex: Int) =
        entities[viewIndex]?.remove(layerIndex)

    protected operator fun get(viewLayer: ViewLayerAware): DynArray<Entity>? =
        entities[viewLayer.viewIndex]?.get(viewLayer.layerIndex)

    protected fun getIfNotEmpty(viewIndex: Int, layerIndex: Int): DynArray<Entity>? {
        val result = entities[viewIndex]?.get(layerIndex)
        return if (result != null && !result.isEmpty) result
            else null
    }

    protected fun collectTransformData(parentId: Int, transformCollector: TransformDataCollector) {
        if (parentId < 0)
            return

        val parent = EntitySystem[parentId]
        val parentTransform = parent[ETransform]
        transformCollector + parentTransform.data
        if (EChild in parent.aspects)
            collectTransformData(parent[EChild].int_parent, transformCollector)
    }

    private fun forceGet(viewLayer: ViewLayerAware): DynArray<Entity>? =
        forceGet(viewLayer.viewIndex, viewLayer.layerIndex)

    private fun forceGet(viewId: Int, layerId: Int): DynArray<Entity>? {
        if (viewId !in entities)
            entities[viewId] = DynArray.of()
        if (layerId !in entities[viewId]!!)
            entities[viewId]?.set(layerId, DynArray.of())

        return entities[viewId]!![layerId]
    }

    abstract fun match(entity: Entity): Boolean

    abstract fun render(viewIndex: Int, layerIndex: Int, clip: Rectangle)

    override fun componentType(): ComponentType<Renderer> = Companion

    companion object : SystemComponentType<Renderer>(Renderer::class)

    protected interface TransformDataCollector {
        val data : TransformData
        operator fun invoke(transform: TransformData)
        operator fun invoke(position: PositionF)
        operator fun set(offset: PositionF, transform: TransformData)
        operator fun plus(transform: TransformData)
        operator fun plus(offset: PositionF)
        operator fun minus(offset: PositionF)
        fun move(dx: Float, dy:Float)
    }

    protected class ExactTransformDataCollector internal constructor() : TransformDataCollector {
        override val data = TransformData()

        override operator fun invoke(transform: TransformData) {
            data.position(transform.position)
            data.pivot(transform.pivot)
            data.scale(transform.scale)
            data.rotation = transform.rotation
        }

        override operator fun invoke(position: PositionF) {
            data.position + position
        }

        override operator fun set(offset: PositionF, transform: TransformData) {
            this(transform)
            data.position + offset
        }

        override operator fun plus(transform: TransformData) {
            data + transform
        }

        override operator fun plus(offset: PositionF) {
            data.position + offset
        }

        override fun minus(offset: PositionF) {
            data.position - offset
        }

        override fun move(dx: Float, dy: Float) {
            data.position.x += dx
            data.position.y += dy
        }
    }

    protected class DiscreteTransformDataCollector internal constructor() : TransformDataCollector {

        override val data = TransformData()

        override operator fun invoke(transform: TransformData) {
            data.position(
                floor(transform.position.x.toDouble()).toFloat(),
                floor(transform.position.y.toDouble()).toFloat()
            )
            data.pivot(
                floor(transform.pivot.x.toDouble()).toFloat(),
                floor(transform.pivot.y.toDouble()).toFloat()
            )
            data.scale(transform.scale)
            data.rotation = transform.rotation
        }

        override operator fun invoke(position: PositionF) {
            data.position(
                floor(position.x.toDouble()).toFloat(),
                floor(position.y.toDouble()).toFloat()
            )
        }

        override operator fun set(offset: PositionF, transform: TransformData) {
            this(transform)
            data.position(
                floor((transform.position.x + offset.x).toDouble()).toFloat(),
                floor((transform.position.y + offset.y).toDouble()).toFloat()
            )
        }

        override operator fun plus(transform: TransformData) {
            data.position.x += floor(transform.position.x.toDouble()).toFloat()
            data.position.y += floor(transform.position.y.toDouble()).toFloat()
            data.pivot.x += floor(transform.pivot.x.toDouble()).toFloat()
            data.pivot.y += floor(transform.pivot.y.toDouble()).toFloat()
            data.scale.dx *= transform.scale.dx
            data.scale.dy *= transform.scale.dy
            data.rotation += transform.rotation
        }

        override operator fun plus(offset: PositionF) {
            data.position.x += floor(offset.x.toDouble()).toFloat()
            data.position.y += floor(offset.y.toDouble()).toFloat()
        }

        override fun minus(offset: PositionF) {
            data.position.x -= floor(offset.x.toDouble()).toFloat()
            data.position.y -= floor(offset.y.toDouble()).toFloat()
        }

        override fun move(dx: Float, dy: Float) {
            data.position.x -= floor(dx.toDouble()).toFloat()
            data.position.y -= floor(dy.toDouble()).toFloat()
        }
    }
}