package com.inari.firefly.control.ai.behavior

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

class EBehavior private constructor() : EntityComponent(EBehavior::class.simpleName!!){

    @JvmField internal var treeRef = -1
    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER

    @JvmField val behaviorTree = ComponentRefResolver(BxNode) { index-> treeRef = index }
    @JvmField var repeat: Boolean = true
    @JvmField var active: Boolean = true
    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }
    var treeState: OpResult = OpResult.SUCCESS
        internal set

    override fun reset() {
        treeRef = -1
        repeat = true
        treeState = OpResult.SUCCESS
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EBehavior>(EBehavior::class) {
        override fun createEmpty() = EBehavior()
    }
}