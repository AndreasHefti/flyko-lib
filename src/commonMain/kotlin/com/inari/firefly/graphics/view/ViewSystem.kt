package com.inari.firefly.graphics.view

import com.inari.firefly.BASE_VIEW
import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.api.ViewData
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.Consumer
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.collection.DynIntArray
import com.inari.util.collection.DynIntArrayRO
import kotlin.jvm.JvmField

object ViewSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(View, Layer)

    val views: ComponentMapRO<View>
        get() = systemViews
    private val systemViews = ComponentSystem.createComponentMapping(
        View,
        activationMapping = true,
        nameMapping = true,
        listener = { view, action -> when (action) {
            CREATED -> created(view)
            ACTIVATED     -> activated(view)
            DEACTIVATED   -> deactivated(view)
            DELETED       -> deleted(view)
        } }
    )

    val layers: ComponentMapRO<Layer>
        get() = systemLayers
    private val systemLayers: ComponentMap<Layer> = ComponentSystem.createComponentMapping(
        Layer,
        activationMapping = true,
        listener = { layer, action -> when (action) {
            CREATED       -> created(layer)
            DELETED       -> deleted(layer)
            else -> {}
        } }
    )

    @JvmField val baseView: View
    @JvmField internal val privateActiveViewPorts: DynArray<ViewData> = DynArray.of(5, 5)
    @JvmField internal val privateLayersOfView: DynArray<DynArray<Layer>> = DynArray.of(5, 5)
    @JvmField val activeViewPorts: DynArrayRO<ViewData> = privateActiveViewPorts
    private val orderedView: DynIntArray = DynIntArray(10, -1, 5)
    private val viewComparator = Comparator<Int> { a, b ->
        systemViews[a].zPosition.compareTo(systemViews[b].zPosition)
    }
    private val layerComparator = Comparator<Layer?> { a, b ->
        a?.zPosition ?: 0.compareTo(b?.zPosition ?: 0)
    }

    init {
        baseView = View.buildAndGet {
            name = BASE_VIEW
            bounds(w=FFContext.screenWidth, h=FFContext.screenHeight)
            baseView = true
        }

        FFContext.loadSystem(this)
    }

    fun layersOfView(viewId: CompId): DynArrayRO<Layer> = privateLayersOfView[viewId.index]!!
    fun layersOfView(viewId: Int): DynArrayRO<Layer> = privateLayersOfView[viewId]!!
    fun layersOfView(viewName: String): DynArrayRO<Layer> = privateLayersOfView[views[viewName].index]!!

    private fun created(view: View) {
        val index = view.index
        if (index !in privateLayersOfView)
            privateLayersOfView[index] = DynArray.of(10, 5)

        if (!view.baseView)
            orderedView.add(index)
        orderedView.sort(viewComparator)

        ViewEvent.send(view.componentId, view.data, ViewEvent.Type.VIEW_CREATED)
    }

    private fun activated(view: View) {
        if (view.baseView)
            return

        updateViewMapping()
        ViewEvent.send(view.componentId, view.data, ViewEvent.Type.VIEW_ACTIVATED)
    }

    private fun deactivated(view: View) {
        if (view.baseView)
            throw IllegalStateException("Base View cannot be deactivated")

        updateViewMapping()
        privateLayersOfView[view.index]?.forEach {
            systemLayers.deactivate(it.componentId)
        }

        ViewEvent.send(view.componentId, view.data, ViewEvent.Type.VIEW_DISPOSED)
    }

    private fun deleted(view: View) {
        if (view.baseView)
            throw IllegalStateException("Base View cannot be deactivated")

        // delete also all layers of this view
        privateLayersOfView[view.index]?.forEach {
            systemLayers.delete(it.componentId)
        }

        orderedView.remove(view.index)
        orderedView.sort(viewComparator)

        ViewEvent.send(view.componentId, view.data, ViewEvent.Type.VIEW_DELETED)
    }

    private fun created(layer: Layer) {
        if (layer.viewRef !in views)
            throw IllegalStateException("No View exists for Layer: $layer")
        privateLayersOfView[layer.viewRef]?.add(layer)
        privateLayersOfView[layer.viewRef]?.sort(layerComparator)
    }

    private fun deleted(layer: Layer) {
        privateLayersOfView[layer.viewRef]?.remove(layer)
        privateLayersOfView[layer.viewRef]?.sort(layerComparator)
    }

    private fun updateViewMapping() {
        privateActiveViewPorts.clear()
        val i = orderedView.iterator()
        while (i.hasNext()) {
            val nextId = i.next()
            if (views.isActive(nextId)) {
                privateActiveViewPorts.add(views[nextId].data)
            }
        }
    }

    override fun clearSystem() {
        var i = 0
        while (i < systemViews.map.capacity) {
            val view = systemViews.map[i++] ?: continue
            if (view.baseView)
                continue

            systemViews.delete(view.index)
        }
        systemLayers.clear()
    }
}