package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.ComponentEventType.*
import com.inari.firefly.core.api.ViewData
import com.inari.firefly.graphics.view.ViewSystemRenderer.disposeViewRenderer
import com.inari.firefly.graphics.view.ViewSystemRenderer.registerViewRenderer
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.collection.DynIntArray
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField
import kotlin.math.floor

interface ViewRenderer {
    val name: String
    val order: Int
    fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i)
    fun init()
    fun dispose()
}

abstract class EntityRenderer(override val name: String) : ViewRenderer {

    @JvmField val transformCollector = ExactTransformDataCollector()

    override var order: Int = 0
        set(value) {
            field = value
            ViewSystemRenderer.sortRenderingChain()
        }

    private val entities: DynArray<DynArray<DynArray<Entity>>> = DynArray.of(10, 2) // { size -> arrayOfNulls(size) }

    private fun entityListener(index: Int, type: ComponentEventType) {
        if (type == ACTIVATED) registerEntity(index)
        else if (type == DEACTIVATED) disposeEntity(index)
    }

    override fun init() {
        registerViewRenderer(this)
        Entity.registerComponentListener(this::entityListener)
    }

    private fun registerEntity(index: Int) {
        val entity = Entity[index]
        if (!acceptEntity(entity))
            return
        val transform = entity[ETransform]
        if (transform.viewIndex !in entities)
            entities[transform.viewIndex] = DynArray.of(10, 2)
        val views = entities[transform.viewIndex]!!
        if (transform.layerIndex !in views)
            views[transform.layerIndex] = DynArray.of() // DynArray(100, 50) { size -> arrayOfNulls(size) }
        val layer = views[transform.layerIndex]!!
        layer.add(entity)
        sort(layer)
    }
    private fun disposeEntity(index: Int) {
        val entity = Entity[index]
        if (!acceptEntity(entity))
            return
        val transform = entity[ETransform]
        if (transform.viewIndex !in entities)
            return
        val views = entities[transform.viewIndex]!!
        if (transform.layerIndex !in views)
            return
        val layer = views[transform.layerIndex]!!
        layer.remove(entity)
        sort(layer)
    }

    protected abstract fun acceptEntity(entity: Entity): Boolean
    protected abstract fun sort(entities: DynArray<Entity>)

    override fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val entitiesToRender = entities[viewIndex]?.get(layerIndex) ?: return
        render(entitiesToRender)
    }

    abstract fun render(entities: DynArray<Entity>)

    protected fun collectTransformData(parentId: Int, transformCollector: TransformDataCollector) {
        if (parentId < 0)
            return

        val parent = Entity[parentId]
        val parentTransform = parent[ETransform]
        transformCollector + parentTransform
        if (EChild in parent.aspects)
            collectTransformData(parent[EChild].parent.targetKey.instanceIndex, transformCollector)
    }

    override fun dispose() {
        Entity.disposeComponentListener(this::entityListener)
        disposeViewRenderer(this)
        entities.clear()
    }
}

object ViewSystemRenderer : Renderer() {

    internal val NO_VIRTUAL_VIEW_PORTS: DynArrayRO<ViewData> = DynArray.of(0,0)
    private val RENDERING_CHAIN = mutableListOf<ViewRenderer>()
    private val SORTED_VIEWS = DynArray.of<ViewData>()
    private val VIEW_LAYER_MAPPING = DynArray.of<Pair<ViewData, DynIntArray>>()
    private val CLIP = Vector4i()
    private val VIEW_COMPARATOR = Comparator<ViewData?> { a, b ->
        val p1 = if (a != null) View[a.index].zPosition else 0
        val p2 = if (b != null) View[b.index].zPosition else 0
        p1.compareTo(p2)
    }
    private val LAYER_COMPARATOR = Comparator<Int> { a, b ->
        Layer[a].zPosition.compareTo(Layer[b].zPosition)
    }
    private val RENDERING_CHAIN_COMPARATOR = Comparator<ViewRenderer> { a, b ->
        a.order.compareTo(b.order)
    }

    private val onlyBaseView: Boolean get() = VIEW_LAYER_MAPPING.size <= 0

    private fun viewListener(index: Int, type: ComponentEventType) {
        if (type == ACTIVATED) VIEW_LAYER_MAPPING[index] = Pair(View[index], DynIntArray())
        else if (type == DEACTIVATED) VIEW_LAYER_MAPPING.remove(index)
        sort()
    }

    private fun layerListener(index: Int, type: ComponentEventType) {
        val viewIndex = Layer[index].view.targetKey.instanceIndex
        if (type == ACTIVATED) VIEW_LAYER_MAPPING[viewIndex]!!.second.add(index)
        else if (type == DEACTIVATED) VIEW_LAYER_MAPPING[viewIndex]!!.second.remove(index)
        sort()
    }

    init {
        super.activate()
        View.registerComponentListener(this::viewListener)
        Layer.registerComponentListener(this::layerListener)
    }

    private fun sort() {
        SORTED_VIEWS.clear()
        VIEW_LAYER_MAPPING.forEach {
            it.second.sort(LAYER_COMPARATOR)
            SORTED_VIEWS.add(it.first)
        }
        SORTED_VIEWS.sort(VIEW_COMPARATOR)
    }

    internal fun sortRenderingChain() {
        RENDERING_CHAIN.sortWith(RENDERING_CHAIN_COMPARATOR)
    }

    fun registerViewRenderer(renderer: ViewRenderer) {
        if (renderer !in RENDERING_CHAIN)
            RENDERING_CHAIN.add(renderer)
        sortRenderingChain()
    }

    @Suppress("UNCHECKED_CAST")
    fun <R: ViewRenderer>byName(rendererName: String): R =
        RENDERING_CHAIN.find { renderer -> renderer.name == rendererName } as R

    fun disposeViewRenderer(renderer: ViewRenderer) {
        if (renderer in RENDERING_CHAIN)
            RENDERING_CHAIN.remove(renderer)
        sortRenderingChain()
    }

    override fun render() {
        if (onlyBaseView) {
            renderViewport(View[View.BASE_VIEW_KEY])
            Engine.graphics.flush(NO_VIRTUAL_VIEW_PORTS)
        } else {
            SORTED_VIEWS.forEach { renderViewport(it) }
            Engine.graphics.flush(SORTED_VIEWS)
        }
    }

    private fun renderViewport(data: ViewData) {

        CLIP(
            floor(data.worldPosition.x.toDouble()).toInt(),
            floor(data.worldPosition.y.toDouble()).toInt(),
            data.bounds.width,
            data.bounds.height
        )

        Engine.graphics.startViewportRendering(data, true)
        val layers = VIEW_LAYER_MAPPING[data.index]?.second
        if (layers != null && !layers.isEmpty) {
            val layerIterator = layers.iterator()
            while (layerIterator.hasNext()) {
                val layerIndex = layerIterator.next()
                Engine.graphics.setActiveShader(Layer[layerIndex].shaderIndex)
                render(data.index, layerIndex, CLIP)
            }
        } else
            render(data.index, 0, CLIP)

        Engine.graphics.endViewportRendering(data)
    }

    private fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) =
        RENDERING_CHAIN.forEach { it.render(viewIndex, layerIndex, clip) }

}