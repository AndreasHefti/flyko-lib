package com.inari.firefly.control.scene

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
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

class Scene private constructor() : GenericComposite() {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var internal_callback: Call = VOID_CALL
        internal set
    @JvmField var removeAfterRun: Boolean = false
    @JvmField internal var runTaskRef = -1

    fun withCallback(callback: Call) {
        internal_callback = {
            FFContext.dispose(componentId)
            callback()
        }
    }

    val withRunTask = ComponentRefResolver(Task) { index -> runTaskRef = index }
    fun <A : Task> withRunTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        runTaskRef = result.instanceId
        return result
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Scene>(Composite, Scene::class) {
        init { CompositeSystem.compositeBuilderMapping[Scene::class.simpleName!!] = this }
        override fun createEmpty() = Scene()
    }
}