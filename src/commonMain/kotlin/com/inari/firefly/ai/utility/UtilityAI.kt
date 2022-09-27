package com.inari.firefly.ai.utility

import com.inari.firefly.core.*
import com.inari.util.*
import com.inari.util.collection.BitSet
import com.inari.util.geom.Easing
import com.inari.util.geom.EasingFunction
import kotlin.jvm.JvmField

abstract class UtilityAI protected constructor() : Component(UtilityAI) {

    abstract  fun getUtilityValue(entityId: Int, intentionId: Int): Float

    companion object : ComponentSystem<UtilityAI>("UtilityAI") {
        override fun allocateArray(size: Int): Array<UtilityAI?> = arrayOfNulls(size)
        override fun create() =
            throw UnsupportedOperationException("UtilityAI is abstract use a concrete implementation instead")

        private val entityIds = BitSet()
        private val entityListener: ComponentEventListener = { key, type ->
            val entity = Entity[key.instanceIndex]
            if (EUtility in entity.aspects) {
                when (type) {
                    ComponentEventType.ACTIVATED -> entityIds[key.instanceIndex] = true
                    ComponentEventType.DEACTIVATED -> entityIds[key.instanceIndex] = false
                    else -> {}
                }
            }
        }

        private fun update() {
            var i = entityIds.nextSetBit(0)
            while (i >= 0) {
                val entity = Entity[i]
                i = entityIds.nextSetBit(i + 1)

                val utility = entity[EUtility]
                if (!utility.scheduler.needsUpdate())
                    return

                if (utility.runningActionIndex >= 0) {
                    val action = this[utility.runningActionIndex] as UtilityAction
                    val result = action.actionOperation(entity.index, action.operationArg2, action.operationArg3)
                    if (result != OperationResult.RUNNING)
                        utility.runningActionIndex = -1
                }

                if (utility.runningActionIndex < 0) {
                    // find next action by intention
                    var maxUtilityValue = 0f
                    var id = -1
                    var intIndex = utility.intentions.nextSetBit(0)
                    while (intIndex >= 0) {
                        val uv = this[intIndex].getUtilityValue(entity.index, -1)
                        if (uv > maxUtilityValue) {
                            maxUtilityValue = uv
                            id = intIndex
                        }
                        intIndex = utility.intentions.nextSetBit(intIndex)
                    }
                    if (intIndex >= 0 && maxUtilityValue >= utility.intentionThreshold) {
                        // get best fitting action for the previous found intention
                        val intention = this[id]
                        id = -1
                        maxUtilityValue = 0f
                        intIndex = utility.actions.nextSetBit(0)
                        while (intIndex >= 0) {
                            val uv = this[intIndex].getUtilityValue(entity.index, intention.index)
                            if (uv > maxUtilityValue) {
                                maxUtilityValue = uv
                                id = intIndex
                            }
                        }
                        if (id >= 0 && maxUtilityValue >= utility.actionThreshold)
                            utility.runningActionIndex = id
                    }
                }
            }
        }

        init {
            Engine.registerListener(Engine.UPDATE_EVENT_TYPE, this::update)
            Entity.registerComponentListener(entityListener)
        }
    }
}

class Consideration private constructor() : UtilityAI() {

    @JvmField val weighting = 1f
    @JvmField val quantifier: EasingFunction = Easing.LINEAR
    @JvmField var normalOperation: NormalOperation = ZERO_OP

    override fun getUtilityValue(entityId: Int, intentionId: Int): Float =
        quantifier(normalOperation(entityId, intentionId)) * weighting

    companion object : ComponentSubTypeBuilder<UtilityAI, Consideration>(UtilityAI, "Consideration") {
        override fun create() = Consideration()
    }
}

class Intention private constructor() : UtilityAI() {

    @JvmField val considerations = BitSet()

    fun withConsideration(key: ComponentKey) {
        considerations[key.instanceIndex] = true
    }
    fun withConsideration(configure: (Consideration.() -> Unit)): ComponentKey {
        val result = Consideration.build(configure)
        considerations.set(result.instanceIndex)
        return result
    }
    fun removeConsideration(key: ComponentKey) {
        considerations[key.instanceIndex] = false
    }

    override fun getUtilityValue(entityId: Int, intentionId: Int): Float {
        var index = considerations.nextSetBit(0)
        var result = 0f
        while (index >= 0) {
            result += UtilityAI[index].getUtilityValue(entityId, this.index)
            index = considerations.nextSetBit(index)
        }
        return result
    }

    companion object : ComponentSubTypeBuilder<UtilityAI, Intention>(UtilityAI, "Intention") {
        override fun create() = Intention()
    }
}

class UtilityAction private constructor() : UtilityAI() {

    @JvmField var actionOperation: TaskOperation = SUCCESS_TASK_OPERATION
    @JvmField var operationArg2 = -1
    @JvmField var operationArg3 = -1
    @JvmField val considerations = BitSet()

    fun withConsideration(key: ComponentKey) {
        considerations[key.instanceIndex] = true
    }
    fun withConsideration(configure: (Consideration.() -> Unit)): ComponentKey {
        val result = Consideration.build(configure)
        considerations.set(result.instanceIndex)
        return result
    }
    fun removeConsideration(key: ComponentKey) {
        considerations[key.instanceIndex] = false
    }

    override fun getUtilityValue(entityId: Int, intentionId: Int): Float {
        var index = considerations.nextSetBit(0)
        var result = 0f
        while (index >= 0) {
            result += UtilityAI[index].getUtilityValue(entityId, intentionId)
            index = considerations.nextSetBit(index)
        }
        return result
    }

    companion object : ComponentSubTypeBuilder<UtilityAI, UtilityAction>(UtilityAI, "UtilityAction") {
        override fun create() = UtilityAction()
    }
}