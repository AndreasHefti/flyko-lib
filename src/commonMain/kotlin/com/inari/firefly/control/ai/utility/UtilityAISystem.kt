package com.inari.firefly.control.ai.utility

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntityEvent
import com.inari.firefly.entity.EntityEventListener
import com.inari.firefly.entity.EntitySystem
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetIterator

object UtilityAISystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
        Consideration,
        Intention,
        UtilityAIAction
    )

    val considerations: ComponentMapRO<Consideration>
        get() = systemConsiderations
    private val systemConsiderations = ComponentSystem.createComponentMapping(
        Consideration,
        nameMapping = true
    )

    val intentions: ComponentMapRO<Intention>
    get() = systemIntention
    private val systemIntention = ComponentSystem.createComponentMapping(
        Intention,
        nameMapping = true
    )

    val actions: ComponentMapRO<UtilityAIAction>
        get() = systemActions
    private val systemActions = ComponentSystem.createComponentMapping(
        UtilityAIAction,
        nameMapping = true
    )

    private val entityIds = BitSet()
    private val entityActivationListener: EntityEventListener = object : EntityEventListener {
        override fun entityActivated(entity: Entity) =
            entityIds.set(entity.index)
        override fun entityDeactivated(entity: Entity) =
            entityIds.clear(entity.index)
        override fun match(aspects: Aspects): Boolean =
            aspects.contains(EUtilityAI)
    }

    init {
        FFContext.loadSystem(this)
        FFContext.registerListener(FFApp.UpdateEvent, this::update)
        FFContext.registerListener(EntityEvent, entityActivationListener)
    }

    internal fun update() {
        val iterator = BitSetIterator(entityIds)
        while (iterator.hasNext())
            tick(iterator.next())
    }

    internal fun tick(entityId: Int) {
        val entity = EntitySystem[entityId]
        val utility = entity[EUtilityAI]
        if (!utility.scheduler.needsUpdate())
            return

        if (utility.runningActionRef >= 0) {
            val action = systemActions[utility.runningActionRef]
            val result = action.actionOperation(entityId, action.operationArg2, action.operationArg3)
            if (result != OpResult.RUNNING)
                utility.runningActionRef = -1
        }

        if (utility.runningActionRef < 0) {
            // TODO find next action by intention
            var maxUtilityValue = 0f
            var id = -1
            var intIndex = utility.intentions.nextSetBit(0)
            while (intIndex >= 0) {
                val uv = intentions[intIndex].getUtilityValue(entityId)
                if (uv > maxUtilityValue) {
                    maxUtilityValue = uv
                    id = intIndex
                }
                intIndex = utility.intentions.nextSetBit(intIndex)
            }
            if (intIndex >= 0 && maxUtilityValue >= utility.intentionThreshold) {
                // get best fitting action for the previous found intention
                val intention = intentions[id]
                id = -1
                maxUtilityValue = 0f
                intIndex = utility.actions.nextSetBit(0)
                while (intIndex >= 0) {
                    val uv = actions[intIndex].getUtilityValue(entityId, intention.index)
                    if (uv > maxUtilityValue) {
                        maxUtilityValue = uv
                        id = intIndex
                    }
                }
                if (id >= 0 && maxUtilityValue >= utility.actionThreshold)
                    utility.runningActionRef = id
            }
        }

    }

    override fun clearSystem() {
        systemConsiderations.clear()
        systemIntention.clear()
        systemActions.clear()
    }

}