package com.inari.firefly.control.ai.utility

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class EUtilityAI private constructor() : EntityComponent(EUtilityAI::class.simpleName!!){

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var intentions = BitSet()
    @JvmField internal val actions = BitSet()

    @JvmField internal var runningActionRef = -1

    @JvmField var intentionThreshold = .5f
    @JvmField var actionThreshold = .5f
    @JvmField var additionalReferences: Array<CompId> = emptyArray()

    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    @JvmField val withIntention = ComponentRefResolver(Intention) { intentions[it] = true }
    fun <A : Intention> withIntention(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        intentions.set(result.index)
        return result
    }
    @JvmField val removeIntention = ComponentRefResolver(Intention) { intentions[it] = false }

    @JvmField val withAction = ComponentRefResolver(UtilityAIAction) { actions[it] = true }
    fun <A : UtilityAIAction> withAction(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        actions.set(result.index)
        return result
    }
    @JvmField val removeAction = ComponentRefResolver(UtilityAIAction) { actions[it] = false }

    override fun reset() {
        runningActionRef = -1
        intentions.clear()
        actions.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EUtilityAI>(EUtilityAI::class) {
        override fun createEmpty() = EUtilityAI()
    }

}