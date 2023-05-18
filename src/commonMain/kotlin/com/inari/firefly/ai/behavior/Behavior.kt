package com.inari.firefly.ai.behavior

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.*
import com.inari.firefly.core.api.ActionResult.*
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class BehaviorNode protected constructor() : Component(BehaviorNode) {

    abstract fun tick(index: EntityIndex): ActionResult

    companion object : AbstractComponentSystem<BehaviorNode>("BehaviorNode") {
        override fun allocateArray(size: Int): Array<BehaviorNode?> = arrayOfNulls(size)

        private val entityIds = BitSet()
        private val entityListener: ComponentEventListener = { key, type ->
            if (key.componentIndex in EBehavior) {
                if (type == ComponentEventType.ACTIVATED)
                    entityIds[key.componentIndex] = true
                else if (type == ComponentEventType.DEACTIVATED)
                    entityIds[key.componentIndex] = false
            }
        }

        private fun update() {
            var i = entityIds.nextSetBit(0)
            while (i >= 0) {
                val behavior = EBehavior[i]
                if (!behavior.active || behavior.behaviorTreeRef.targetKey.componentIndex < 0 || !behavior.scheduler.needsUpdate())
                    return

                if (behavior.treeState === SUCCESS)
                    if (!behavior.repeat)
                        return
                    else
                        reset(i)

                behavior.treeState = BehaviorNode[behavior.treeIndex].tick(i)
                i = entityIds.nextSetBit(i + 1)
            }
        }

        fun reset(index: EntityIndex) {
            EBehavior[index].treeState = SUCCESS
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

    override fun tick(index: EntityIndex): ActionResult {
        val threshold = if (successThreshold > childrenNodes.size)
            childrenNodes.size
        else
            successThreshold

        var successCount = 0
        var failuresCount = 0
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(index)
            if (result == SUCCESS) successCount++
            else if (result == FAILED) failuresCount++
            i = childrenNodes.nextSetBit(i + 1)
        }
        return if (successCount >= threshold) SUCCESS
        else if (failuresCount > 0 ) FAILED
        else RUNNING
    }

    companion object : SubComponentBuilder<BehaviorNode, ParallelNode>(BehaviorNode) {
        override fun create() = ParallelNode()
    }
}

class SelectionNode private constructor() : BranchNode() {

    override fun tick(index: EntityIndex): ActionResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(index)
            if (result != FAILED)
                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return FAILED
    }

    companion object : SubComponentBuilder<BehaviorNode, SelectionNode>(BehaviorNode) {
        override fun create() = SelectionNode()
    }
}

class SequenceNode private constructor() : BranchNode() {

    override fun tick(index: EntityIndex): ActionResult {
        var i = childrenNodes.nextSetBit(0)
        loop@ while (i >= 0) {
            val result = BehaviorNode[i].tick(index)
            if (result != SUCCESS)
                return result
            i = childrenNodes.nextSetBit(i + 1)
        }
        return SUCCESS
    }

    companion object : SubComponentBuilder<BehaviorNode, SequenceNode>(BehaviorNode) {
        override fun create() = SequenceNode()
    }
}

class ActionNode private constructor() : BehaviorNode() {

    @JvmField var condition: (Int) -> Boolean = { true }
    @JvmField var actionOperation: (Int) -> ActionResult = { SUCCESS }

    override fun tick(index: EntityIndex): ActionResult =
        if (condition(index))
            actionOperation(index)
        else FAILED


    companion object : SubComponentBuilder<BehaviorNode, ActionNode>(BehaviorNode) {
        override fun create() = ActionNode()
    }
}
