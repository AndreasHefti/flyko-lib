package com.inari.firefly.composite

import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.game.player.PlayerSystem
import kotlin.jvm.JvmField

class EComposite private constructor () : EntityComponent(EComposite::class.simpleName!!) {

    @JvmField internal var loadTaskRef = -1
    @JvmField internal var activationTaskRef = -1
    @JvmField internal var deactivationTaskRef = -1
    @JvmField internal var disposeTaskRef = -1
    @JvmField internal val attributes = mutableMapOf<String, String>()

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    @JvmField val withLoadTask = ComponentRefResolver(Task) { index -> PlayerSystem.loadTaskRef + index }
    fun <A : Task> withLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.loadTaskRef = result.instanceId
        return result
    }

    @JvmField val withActivationTask = ComponentRefResolver(Task) { index -> PlayerSystem.activationTaskRef + index }
    fun <A : Task> withActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.activationTaskRef = result.instanceId
        return result
    }

    @JvmField val withDeactivationTask = ComponentRefResolver(Task) { index -> PlayerSystem.deactivationTaskRef + index }
    fun <A : Task> withDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.deactivationTaskRef = result.instanceId
        return result
    }

    @JvmField val withDisposeTask = ComponentRefResolver(Task) { index -> PlayerSystem.unloadTaskRef + index }
    fun <A : Task> withDisposeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.unloadTaskRef = result.instanceId
        return result
    }

    override fun reset() {
        loadTaskRef = -1
        activationTaskRef = -1
        deactivationTaskRef = -1
        disposeTaskRef = -1
        attributes.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EComposite>(EComposite::class) {
        override fun createEmpty() = EComposite()
    }
}