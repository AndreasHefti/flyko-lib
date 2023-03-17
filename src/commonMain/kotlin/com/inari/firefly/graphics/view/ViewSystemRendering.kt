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
import kotlin.native.concurrent.ThreadLocal

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

    private val entities: DynArray<DynArray<DynArray<Entity>>> = DynArray.of(10, 2)

    private fun entityListener(key: ComponentKey, type: ComponentEventType) {
        if (type == ACTIVATED) registerEntity(key.componentIndex)
        else if (type == DEACTIVATED) disposeEntity(key.componentIndex)
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
            views[transform.layerIndex] = DynArray.of()
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
            collectTransformData(parent[EChild].parent.targetKey.componentIndex, transformCollector)
    }

    override fun dispose() {
        Entity.disposeComponentListener(this::entityListener)
        disposeViewRenderer(this)
        entities.clear()
    }
}

object ViewSystemRenderer : Renderer() {

    private val RENDERING_CHAIN: DynArray<ViewRenderer> = DynArray.of(5)
    private val CLIP = Vector4i()

    // TODO why is this really needed?
    internal val NO_VIRTUAL_VIEW_PORTS: DynArrayRO<ViewData> = DynArray.of(0,0)
    private val VIEW_LAYER_MAPPING = DynArray.of<Pair<View, DynIntArray>>(5, 10)
    private val SORTED_RENDER_TO_BASE = DynArray.of<ViewData>(5, 10)
    private val FOR_ENTITY_RENDERING = DynArray.of<ViewData>(5, 10)

    private val VIEW_COMPARATOR = Comparator<ViewData?> { a, b ->
        val p1 = if (a != null) View[a.index].zPosition else 0
        val p2 = if (b != null) View[b.index].zPosition else 0
        p1.compareTo(p2)
    }
    private val LAYER_COMPARATOR = Comparator<Int> { a, b ->
        Layer[a].zPosition.compareTo(Layer[b].zPosition)
    }
    private val RENDERING_CHAIN_COMPARATOR = Comparator<ViewRenderer?> { a, b ->
        a?.order?.compareTo(b?.order ?: 0) ?: 0
    }

    private val onlyBaseView: Boolean get() = VIEW_LAYER_MAPPING.size <= 0

    private fun viewListener(key: ComponentKey, type: ComponentEventType) {
        if (type == ACTIVATED) VIEW_LAYER_MAPPING[key.componentIndex] = Pair(View[key.componentIndex], DynIntArray(2, -1))
        else if (type == DEACTIVATED) VIEW_LAYER_MAPPING.remove(key.componentIndex)
        updateReferences()
    }

    private fun layerListener(key: ComponentKey, type: ComponentEventType) {
        val viewIndex = Layer[key.componentIndex].viewRef.targetKey.componentIndex
        if (type == ACTIVATED) VIEW_LAYER_MAPPING[viewIndex]!!.second.add(key.componentIndex)
        else if (type == DEACTIVATED) VIEW_LAYER_MAPPING[viewIndex]!!.second.remove(key.componentIndex)
        updateReferences()
    }

    init {
        super.activate()
        View.registerComponentListener(this::viewListener)
        Layer.registerComponentListener(this::layerListener)
    }

    private fun updateReferences() {
        SORTED_RENDER_TO_BASE.clear()
        FOR_ENTITY_RENDERING.clear()
        val iter = VIEW_LAYER_MAPPING.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            it.second.sort(LAYER_COMPARATOR)
            if (it.first.renderToBase)
                SORTED_RENDER_TO_BASE.add(it.first)
            if (!it.first.excludeFromEntityRendering)
                FOR_ENTITY_RENDERING.add(it.first)
        }
        SORTED_RENDER_TO_BASE.trim()
        SORTED_RENDER_TO_BASE.sort(VIEW_COMPARATOR)
        FOR_ENTITY_RENDERING.trim()
    }

    internal fun sortRenderingChain() {
        RENDERING_CHAIN.sort(RENDERING_CHAIN_COMPARATOR)
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
            val iter = FOR_ENTITY_RENDERING.iterator()
            while (iter.hasNext())
                renderViewport(iter.next())
            Engine.graphics.flush(SORTED_RENDER_TO_BASE)
        }
    }

    private fun renderViewport(data: ViewData) {

        CLIP(
            floor(data.worldPosition.x).toInt(),
            floor(data.worldPosition.y).toInt(),
            data.bounds.width,
            data.bounds.height
        )

        Engine.graphics.startViewportRendering(data)
        val layers = VIEW_LAYER_MAPPING[data.index]?.second
        if (layers != null && !layers.isEmpty) {
            val layerIterator = layers.iterator()
            while (layerIterator.hasNext()) {
                val layerIndex = layerIterator.next()
                val layer = Layer[layerIndex]

                // apply layer offset
                Engine.graphics.applyViewportOffset(-layer.position.x, -layer.position.y)
                CLIP.x -= layer.position.x.toInt()
                CLIP.y -= layer.position.y.toInt()

                // apply layer shader
                if (layer.shaderIndex >= 0)
                    Engine.graphics.setActiveShader(layer.shaderIndex)

                // render layer
                render(data.index, layerIndex, CLIP)

                // reset layer offset
                Engine.graphics.applyViewportOffset(layer.position.x, layer.position.y)
                CLIP.x += layer.position.x.toInt()
                CLIP.y += layer.position.y.toInt()
            }
        } else
            render(data.index, 0, CLIP)

        Engine.graphics.endViewportRendering(data)
    }

    private fun render(viewIndex: Int, layerIndex: Int, clip: Vector4i) {
        val iter = RENDERING_CHAIN.iterator()
        while (iter.hasNext())
            iter.next().render(viewIndex, layerIndex, clip)
    }
}