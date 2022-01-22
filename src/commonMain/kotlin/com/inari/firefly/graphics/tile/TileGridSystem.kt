package com.inari.firefly.graphics.tile

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFContext
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.EMultiplier
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.graphics.view.*
import com.inari.firefly.graphics.view.ViewEvent.Type.VIEW_DELETED
import com.inari.util.Consumer
import com.inari.util.aspect.Aspects
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import kotlin.jvm.JvmField


object TileGridSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(TileGrid)

    @JvmField val viewLayerListMapping: ViewLayerListMapping<TileGrid> = ViewLayerListMapping.of()
    @JvmField val grids = ComponentSystem.createComponentMapping(
        TileGrid,
        listener = { grid, action -> when (action) {
            CREATED -> viewLayerListMapping.add(grid)
            DELETED -> viewLayerListMapping.delete(grid)
            else -> DO_NOTHING
        } }
    )

    private val viewListener: Consumer<ViewEvent> = { e ->
        when(e.type) {
            VIEW_DELETED -> viewLayerListMapping.deleteAll(e.id.instanceId)
            else -> DO_NOTHING
        }
    }

    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) = addEntity(entity)
        override fun entityDeactivated(entity: Entity) = removeEntity(entity)
        override fun match(aspects: Aspects) = aspects.contains(ETile)
    }

    init {
        FFContext.registerListener(ViewEvent, viewListener)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.loadSystem(this)
    }

    fun existsAny(view: Int, layer: Int): Boolean =
        viewLayerListMapping.contains(view, layer)

    fun existsAny(viewLayer: ViewLayerAware): Boolean =
        viewLayer in viewLayerListMapping

    operator fun get(viewLayer: ViewLayerAware): DynArrayRO<TileGrid>? =
        this[viewLayer.viewIndex, viewLayer.layerIndex]

    operator fun get(viewIndex: Int, layerIndex: Int): DynArrayRO<TileGrid>? =
        viewLayerListMapping[viewIndex, layerIndex]

    override fun clearSystem() {
        grids.clear()
    }

    private fun addEntity(entity: Entity) {
        val tile = entity[ETile]
        val tileGrid = if (tile.tileGridRef >= 0)
            grids[tile.tileGridRef]
        else
            this[entity[ETransform]]?.get(0) ?: return

        if (entity.has(EMultiplier)) {
            val multiplier = entity[EMultiplier]
            val pi = multiplier.positions.iterator()
            while (pi.hasNext()) {
                val x: Int = pi.nextFloat().toInt()
                val y: Int = pi.nextFloat().toInt()
                tileGrid[x, y] = entity.index
            }
        } else {
            tileGrid[tile.position] = entity.index
        }
    }

    private fun removeEntity(entity: Entity) {
        val tile = entity[ETile]
        val tileGrid = if (tile.tileGridRef >= 0)
            if (grids.contains(tile.tileGridRef))
                grids[tile.tileGridRef]
            else return
        else
            this[entity[ETransform]]?.get(0) ?: return

        if (entity.has(EMultiplier)) {
            val multiplier = entity[EMultiplier]
            val pi = multiplier.positions.iterator()
            while (pi.hasNext()) {
                val x: Int = pi.nextFloat().toInt()
                val y: Int = pi.nextFloat().toInt()
                tileGrid.resetIfMatch(entity.index, x, y)
            }
        } else {
            tileGrid.resetIfMatch(entity.index, tile.position)
        }
    }
}