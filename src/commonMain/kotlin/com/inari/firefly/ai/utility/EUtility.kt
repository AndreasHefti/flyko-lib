package com.inari.firefly.ai.utility

import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class EUtility  private constructor() : EntityComponent(EUtility) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var intentions = BitSet()
    @JvmField internal val actions = BitSet()

    @JvmField internal var runningActionIndex = NULL_COMPONENT_INDEX

    @JvmField var intentionThreshold = .5f
    @JvmField var actionThreshold = .5f
    @JvmField var additionalReferences: Array<ComponentKey> = emptyArray()

    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }

    fun withIntention(key: ComponentKey) {
        intentions[key.componentIndex] = true
    }
    fun withIntention(configure: (Intention.() -> Unit)): ComponentKey {
        val result = Intention.build(configure)
        intentions.set(result.componentIndex)
        return result
    }
    fun removeIntention(key: ComponentKey) {
        intentions[key.componentIndex] = false
    }

    fun withAction(key: ComponentKey) {
        actions[key.componentIndex] = true
    }
    fun withAction(configure: (UtilityAction.() -> Unit)): ComponentKey {
        val result = UtilityAction.build(configure)
        actions.set(result.componentIndex)
        return result
    }
    fun removeAction(key: ComponentKey) {
        actions[key.componentIndex] = false
    }

    override fun reset() {
        runningActionIndex = NULL_COMPONENT_INDEX
        intentions.clear()
        actions.clear()
    }

    override val componentType = EUtility
    companion object : EntityComponentBuilder<EUtility>("EUtility") {
        override fun create() = EUtility()
    }
}