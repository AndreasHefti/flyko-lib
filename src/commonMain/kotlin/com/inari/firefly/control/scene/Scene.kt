package com.inari.firefly.control.scene

import com.inari.firefly.EMPTY_TASK_OPERATION
import com.inari.firefly.FFContext
import com.inari.firefly.INFINITE_SCHEDULER
import com.inari.firefly.VOID_CALL
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.control.task.Task
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.Call
import com.inari.util.UpdateOperation
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class Scene private constructor() : GenericComposite() {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var activateTaskRef = -1
    @JvmField internal var deactivateTaskRef = -1
    @JvmField internal var update: UpdateOperation = EMPTY_TASK_OPERATION
    @JvmField internal var callback: Call = VOID_CALL
    @JvmField var removeAfterRun: Boolean = false

    var updateResolution: Float
        get() = throw UnsupportedOperationException()
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }

    fun withCallback(callback: Call) {
        this.callback = callback
    }

    fun withUpdate(update: UpdateOperation) {
        this.update = update
    }

    val withActivateTask = ComponentRefResolver(Task) { index -> activateTaskRef = index }
    fun <A : Task> withActivateTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        activateTaskRef = result.instanceId
        return result
    }

    val withDeactivateTask = ComponentRefResolver(Task) { index -> deactivateTaskRef = index }
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