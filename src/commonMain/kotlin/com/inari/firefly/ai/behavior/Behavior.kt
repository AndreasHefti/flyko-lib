package com.inari.firefly.ai.behavior

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.Action
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.core.api.OperationResult.*
import com.inari.firefly.core.api.SUCCESS_ACTION
import com.inari.util.DO_NOTHING
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class BehaviorNode protected constructor() : Component(BehaviorNode) {

    abstract fun tick(entityId: Int): OperationResult

    companion object : ComponentSystem<BehaviorNode>("BehaviorNode") {
        override fun allocateArray(size: Int): Array<BehaviorNode?> = arrayOfNulls(size)
        override fun create() =
            throw UnsupportedOperationException("BehaviorNode is abstract use a concrete implementation instead")

        private val entityIds = BitSet()
        private val entityListener: ComponentEventListener = { key, type ->
            val entity = Entity[key.componentIndex]
            if (EBehavior in entity.aspects) {
                when (type) {
                    ComponentEventType.ACTIVATED -> entityIds[key.componentIndex] = true
                    ComponentEventType.DEACTIVATED -> entityIds[key.componentIndex] = false
                    else -> {}
                }
//                if (type == ComponentEventType.ACTIVATED)
//                    entityIds[key.componentIndex] = true
//                else if (type == ComponentEventType.DEACTIVATED)
//                    entityIds[key.componentIndex] = false
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

                behavior.treeState = BehaviorNode[behavior.treeIndex].tick(entity.index)
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

abstract class BranchNode internal constructor() : BehaviorNode() {

    @JvmField internal val childrenNodes = BitSet()

    fun <C : BehaviorNode> node(cBuilder: ComponentBuilder<C>, configure: (C.() -> Unit)): ComponentKey {
        val key = cBuilder.build(configure)
        childrenNodes[key.componentIndex] = true
        return key
    }
}

class ParallelNode private constructor() : BranchNode() {

    @JvmField var successThreshold: Int = 0

    override fun tick(entityId: Int): OperationResult {
        val threshold = if (successThreshold > childrenNodes.size)
            childrenNodes.size
        else
            successThreshold

        var successCount = 0
        var failuresCount = 0
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            when(BehaviorNode[i].tick(entityId)) {
                RUNNING -> DO_NOTHING
                SUCCESS -> successCount++
                FAILED -> failuresCount++
            }
//            val result = BehaviorNode[i].tick(entityId)
//            if (result == SUCCESS) successCount++
//            else if (result == FAILED) failuresCount++
            i = childrenNodes.nextSetBit(i + 1)
        }

        return when {
            successCount >= threshold -> SUCCESS
            failuresCount > 0 -> FAILED
            else -> RUNNING
        }

//        return if (successCount >= threshold) SUCCESS
//        else if (failuresCount > 0 ) FAILED
//        else RUNNING
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, ParallelNode>(BehaviorNode, "ParallelNode") {
        override fun create() = ParallelNode()
    }
}

class SelectionNode private constructor() : BranchNode() {

    override fun tick(entityId: Int): OperationResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            when(BehaviorNode[i].tick(entityId)) {
                RUNNING -> return RUNNING
                SUCCESS -> return SUCCESS
                FAILED -> DO_NOTHING
            }
//            val result = BehaviorNode[i].tick(entityId)
//            if (result != FAILED)
//                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return FAILED
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, SelectionNode>(BehaviorNode, "SelectionNode") {
        override fun create() = SelectionNode()
    }
}

class SequenceNode private constructor() : BranchNode() {

    override fun tick(entityId: Int): OperationResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            when(BehaviorNode[i].tick(entityId)) {
                RUNNING -> return RUNNING
                FAILED -> return FAILED
                SUCCESS -> DO_NOTHING
            }
//            val result = BehaviorNode[i].tick(entityId)
//            if (result != SUCCESS)
//                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return SUCCESS
    }

    companion object : ComponentSubTypeBuilder<BehaviorNode, SequenceNode>(BehaviorNode, "SequenceNode") {
        override fun create() = SequenceNode()
    }
}

//class ConditionNode private constructor() : BehaviorNode() {
//
//    @JvmField var condition: Action = SUCCESS_ACTION
//
//    override fun tick(entityId: Int): OperationResult = condition(entityId)
//
//    companion object : ComponentSubTypeBuilder<BehaviorNode, ConditionNode>(BehaviorNode, "ConditionNode") {
//        override fun create() = ConditionNode()
//    }
//}

class ActionNode private constructor() : BehaviorNode() {

    @JvmField var actionOperation: Action = SUCCESS_ACTION

    override fun tick(entityId: Int): OperationResult = actionOperation(entityId)

    companion object : ComponentSubTypeBuilder<BehaviorNode, ActionNode>(BehaviorNode, "ActionNode") {
        override fun create() = ActionNode()
    }
}