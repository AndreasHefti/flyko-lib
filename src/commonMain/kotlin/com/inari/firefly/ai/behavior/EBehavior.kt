package com.inari.firefly.ai.behavior

import com.inari.firefly.core.CReference
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.core.api.FFTimer
import com.inari.util.OperationResult
import kotlin.jvm.JvmField

class EBehavior private constructor() : EntityComponent(EBehavior) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER

    @JvmField val behaviorTreeRef = CReference(BehaviorNode)
    val treeIndex: Int
        get() = behaviorTreeRef.targetKey.instanceIndex
    @JvmField var repeat: Boolean = true
    @JvmField var active: Boolean = true
    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }
    var treeState: OperationResult = OperationResult.SUCCESS
        internal set

    override fun reset() {
        behaviorTreeRef.reset()
        repeat = true
        treeState = OperationResult.SUCCESS
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EBehavior>("EBehavior") {
        override fun create() = EBehavior()
    }
}