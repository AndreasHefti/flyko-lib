package com.inari.firefly.ai.behavior

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.OperationResult.*
import kotlin.jvm.JvmField

class EBehavior private constructor() : EntityComponent(EBehavior) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER

    @JvmField val behaviorTreeRef = CReference(BehaviorNode)
    val treeIndex: Int
        get() = behaviorTreeRef.targetKey.componentIndex
    @JvmField var repeat: Boolean = true
    @JvmField var active: Boolean = true
    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }
    var treeState = SUCCESS
        internal set

    override fun reset() {
        behaviorTreeRef.reset()
        repeat = true
        treeState = SUCCESS
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EBehavior>("EBehavior") {
        override fun create() = EBehavior()
    }
}