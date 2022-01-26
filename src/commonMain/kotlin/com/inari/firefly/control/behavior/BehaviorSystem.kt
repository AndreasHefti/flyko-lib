package com.inari.firefly.control.behavior

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.control.BxConditionOp
import com.inari.firefly.control.BxOp
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.firefly.entity.EntitySystem
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetIterator
import kotlin.jvm.JvmField

object BehaviorSystem : ComponentSystem {

    override val supportedComponents: Aspects =
            SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(BxNode)

    val nodes: ComponentMapRO<BxNode>
        get() = systemNodes
    @JvmField internal val systemNodes = ComponentSystem.createComponentMapping(
            BxNode
    )

    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) =
            entityIds.set(entity.index)
        override fun entityDeactivated(entity: Entity) =
            entityIds.clear(entity.index)
        override fun match(aspects: Aspects): Boolean =
            aspects.contains(EBehavior)
    }

    private val entityIds = BitSet()

    init {
        FFContext.registerListener(FFApp.UpdateEvent, this::update)
        FFContext.registerListener(EntityEvent, entityActivationListener)
        FFContext.loadSystem(this)
    }

    fun reset(entityId: Int) {
        val behavior = EntitySystem[entityId][EBehavior]
        behavior.treeState = OpResult.SUCCESS
        behavior.actionsDone.clear()
    }

    internal fun update() {
        val iterator = BitSetIterator(entityIds)
        while (iterator.hasNext())
            tick(iterator.next())
    }

    internal fun tick(entityId: Int) {
            val entity = EntitySystem[entityId]
            val behavior = entity[EBehavior]
            if (!behavior.active || behavior.treeRef < 0 || !behavior.scheduler.needsUpdate())
                return

            if (behavior.treeState === OpResult.SUCCESS)
                if (!behavior.repeat)
                    return
                else
                    reset(entityId)

            behavior.treeState = systemNodes[behavior.treeRef].tick(entity, behavior)

    }

    override fun clearSystem() {
        systemNodes.clear()
    }
}