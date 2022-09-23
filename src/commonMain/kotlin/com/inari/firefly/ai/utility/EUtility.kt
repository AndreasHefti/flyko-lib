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
        intentions[key.instanceIndex] = true
    }
    fun <A : Intention> withIntention(cBuilder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val result = cBuilder.build(configure)
        intentions.set(result.instanceIndex)
        return result
    }
    fun removeIntention(key: ComponentKey) {
        intentions[key.instanceIndex] = false
    }

    fun withAction(key: ComponentKey) {
        actions[key.instanceIndex] = true
    }
    fun <A : UtilityAction> withAction(cBuilder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val result = cBuilder.build(configure)
        actions.set(result.instanceIndex)
        return result
    }
    fun removeAction(key: ComponentKey) {
        actions[key.instanceIndex] = false
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