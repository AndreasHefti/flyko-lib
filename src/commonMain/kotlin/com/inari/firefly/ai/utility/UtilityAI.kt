package com.inari.firefly.ai.utility

import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.firefly.core.api.ActionResult.RUNNING
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

        private fun update() {
            var i = EUtility.activeComponents.nextSetBit(0)
            while (i >= 0) {
                val utility = EUtility[i]
                if (!utility.scheduler.needsUpdate()) {
                    i = EUtility.activeComponents.nextSetBit(i + 1)
                    continue
                }

                if (utility.runningActionIndex >= 0) {
                    val action = this[utility.runningActionIndex] as UtilityAction
                    val result = action.callAction(i)
                    if (result != RUNNING)
                        utility.runningActionIndex = NULL_COMPONENT_INDEX
                }

                if (utility.runningActionIndex < 0) {
                    // find next action by intention
                    var maxUtilityValue = 0f
                    var id = NULL_COMPONENT_INDEX
                    var intIndex = utility.intentions.nextSetBit(0)
                    while (intIndex >= 0) {
                        val uv = this[intIndex].getUtilityValue(i, NULL_COMPONENT_INDEX)
                        if (uv > maxUtilityValue) {
                            maxUtilityValue = uv
                            id = intIndex
                        }
                        intIndex = utility.intentions.nextSetBit(intIndex)
                    }
                    if (intIndex >= 0 && maxUtilityValue >= utility.intentionThreshold) {
                        // get best fitting action for the previous found intention
                        val intention = this[id]
                        id = NULL_COMPONENT_INDEX
                        maxUtilityValue = 0f
                        intIndex = utility.actions.nextSetBit(0)
                        while (intIndex >= 0) {
                            val uv = this[intIndex].getUtilityValue(i, intention.index)
                            if (uv > maxUtilityValue) {
                                maxUtilityValue = uv
                                id = intIndex
                            }
                        }
                        if (id >= 0 && maxUtilityValue >= utility.actionThreshold)
                            utility.runningActionIndex = id
                    }
                }
                i = EUtility.activeComponents.nextSetBit(i + 1)
            }
        }

        init {
            Engine.registerListener(Engine.UPDATE_EVENT_TYPE, this::update)
        }
    }
}

class Consideration private constructor() : UtilityAI() {

    @JvmField val weighting = 1f
    @JvmField val quantifier: EasingFunction = Easing.LINEAR
    @JvmField var normalOperation: NormalOperation = ZERO_OP

    override fun getUtilityValue(entityId: Int, intentionId: Int): Float =
        quantifier(normalOperation(entityId, intentionId, NULL_COMPONENT_INDEX)) * weighting

    companion object : SubComponentBuilder<UtilityAI, Consideration>(UtilityAI) {
        override fun create() = Consideration()
    }
}

class Intention private constructor() : UtilityAI() {

    @JvmField val considerations = BitSet()

    fun withConsideration(key: ComponentKey) {
        considerations[key.componentIndex] = true
    }
    fun withConsideration(configure: (Consideration.() -> Unit)): ComponentKey {
        val result = Consideration.build(configure)
        considerations.set(result.componentIndex)
        return result
    }
    fun removeConsideration(key: ComponentKey) {
        considerations[key.componentIndex] = false
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

    companion object : SubComponentBuilder<UtilityAI, Intention>(UtilityAI) {
        override fun create() = Intention()
    }
}

class UtilityAction private constructor() : UtilityAI() {

    @JvmField var actionOperation: Action = VOID_ACTION
    @JvmField val considerations = BitSet()

    fun withConsideration(key: ComponentKey) {
        considerations[key.componentIndex] = true
    }
    fun withConsideration(configure: (Consideration.() -> Unit)): ComponentKey {
        val result = Consideration.build(configure)
        considerations.set(result.componentIndex)
        return result
    }
    fun removeConsideration(key: ComponentKey) {
        considerations[key.componentIndex] = false
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

    fun callAction(entityKey: ComponentKey): ActionResult = actionOperation(entityKey.componentIndex)
    fun callAction(index: EntityIndex): ActionResult = actionOperation(index)

    companion object : SubComponentBuilder<UtilityAI, UtilityAction>(UtilityAI) {
        override fun create() = UtilityAction()
    }
}