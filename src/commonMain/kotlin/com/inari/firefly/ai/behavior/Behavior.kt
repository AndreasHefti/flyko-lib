package com.inari.firefly.ai.behavior

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.Action
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.core.api.OperationResult.*
import com.inari.firefly.core.api.SUCCESS_ACTION
import com.inari.firefly.core.api.TRUE_CONDITION
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class BehaviorNode protected constructor(nodeType: ComponentType<out BehaviorNode>) : Component(nodeType) {

    abstract fun tick(entityKey: ComponentKey): OperationResult

    companion object : AbstractComponentSystem<BehaviorNode>("BehaviorNode") {
        override fun allocateArray(size: Int): Array<BehaviorNode?> = arrayOfNulls(size)

        private val entityIds = BitSet()
        private val entityListener: ComponentEventListener = { key, type ->
            val entity = Entity[key]
            if (EBehavior in entity.aspects) {
                if (type == ComponentEventType.ACTIVATED)
                    entityIds[key.componentIndex] = true
                else if (type == ComponentEventType.DEACTIVATED)
                    entityIds[key.componentIndex] = false
            }
        }

        private fun update() {
            var i = entityIds.nextSetBit(0)
            while (i >= 0) {
                val entity = Entity[i]
                i = entityIds.nextSetBit(i + 1)

                val behavior = entity[EBehavior]
                if (!behavior.active || behavior.treeIndex < 0 || !behavior.scheduler.needsUpdate())
                    return

                if (behavior.treeState === SUCCESS)
                    if (!behavior.repeat)
                        return
                    else
                        reset(entity.index)

                behavior.treeState = BehaviorNode[behavior.treeIndex].tick(entity.key)
            }
        }

        fun reset(entityId: Int) {
            val behavior = Entity[entityId][EBehavior]
            behavior.treeState = SUCCESS
        }

        init {
            Engine.registerListener(UPDATE_EVENT_TYPE, this::update)
            Entity.registerComponentListener(entityListener)
        }
    }
}

abstract class BranchNode internal constructor(nodeType: ComponentType<out BranchNode>) : BehaviorNode(nodeType) {

    @JvmField internal val childrenNodes = BitSet()

    fun <C : BehaviorNode> node(cBuilder: ComponentBuilder<C>, configure: (C.() -> Unit)): ComponentKey {
        val key = cBuilder.build(configure)
        childrenNodes[key.componentIndex] = true
        return key
    }
}

class ParallelNode private constructor() : BranchNode(ParallelNode) {

    @JvmField var successThreshold: Int = 0

    override fun tick(entityKey: ComponentKey): OperationResult {
        val threshold = if (successThreshold > childrenNodes.size)
            childrenNodes.size
        else
            successThreshold

        var successCount = 0
        var failuresCount = 0
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(entityKey)
            if (result == SUCCESS) successCount++
            else if (result == FAILED) failuresCount++
            i = childrenNodes.nextSetBit(i + 1)
        }
        return if (successCount >= threshold) SUCCESS
        else if (failuresCount > 0 ) FAILED
        else RUNNING
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, ParallelNode>(BehaviorNode, "ParallelNode") {
        override fun create() = ParallelNode()
    }
}

class SelectionNode private constructor() : BranchNode(SelectionNode) {

    override fun tick(entityKey: ComponentKey): OperationResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(entityKey)
            if (result != FAILED)
                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return FAILED
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, SelectionNode>(BehaviorNode, "SelectionNode") {
        override fun create() = SelectionNode()
    }
}

class SequenceNode private constructor() : BranchNode(SequenceNode) {

    override fun tick(entityKey: ComponentKey): OperationResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(entityKey)
            if (result != SUCCESS)
                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return SUCCESS
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, SequenceNode>(BehaviorNode, "SequenceNode") {
        override fun create() = SequenceNode()
    }
}

class ActionNode private constructor() : BehaviorNode(ActionNode) {

    @JvmField var condition = TRUE_CONDITION
    @JvmField var actionOperation: Action = SUCCESS_ACTION

    override fun tick(entityKey: ComponentKey): OperationResult =
        if (condition(entityKey, NO_COMPONENT_KEY))
            actionOperation(entityKey)
        else FAILED


    companion object : ComponentSubTypeBuilder<BehaviorNode, ActionNode>(BehaviorNode, "ActionNode") {
        override fun create() = ActionNode()
    }
}