package com.inari.firefly.ai.utility

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.api.FFTimer
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class EUtility  private constructor() : EntityComponent(EUtility) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var intentions = BitSet()
    @JvmField internal val actions = BitSet()

    @JvmField internal var runningActionIndex = -1

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
        runningActionIndex = -1
        intentions.clear()
        actions.clear()
    }

    override val componentType = EUtility
    companion object : EntityComponentBuilder<EUtility>("EUtility") {
        override fun create() = EUtility()
    }
}