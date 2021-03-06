package com.inari.firefly.control.scene

import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.DO_NOTHING
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.control.EMPTY_UPDATE_OP
import com.inari.firefly.control.UpdateOperation
import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.Call
import kotlin.jvm.JvmField

class Scene private constructor() : GenericComposite() {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var activateTaskRef = -1
    @JvmField internal var deactivateTaskRef = -1
    @JvmField internal var update: UpdateOperation = EMPTY_UPDATE_OP
    @JvmField internal var callback: Call = DO_NOTHING
    @JvmField var removeAfterRun: Boolean = false

    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    fun withCallback(callback: Call) {
        this.callback = callback
    }

    fun withUpdate(update: UpdateOperation) {
        this.update = update
    }

    @JvmField val withActivateTask = ComponentRefResolver(Task) { activateTaskRef = it }
    fun <A : Task> withActivateTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        activateTaskRef = result.instanceId
        return result
    }

    @JvmField val withDeactivateTask = ComponentRefResolver(Task) { index -> deactivateTaskRef = index }
    fun <A : Task> withDeactivateTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        deactivateTaskRef = result.instanceId
        return result
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Scene>(Composite, Scene::class) {
        init { CompositeSystem.compositeBuilderMapping[Scene::class.simpleName!!] = this }
        override fun createEmpty() = Scene()
    }
}