package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.control.task.ComponentTask
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.player.PlayerSystem
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

open class GenericComposite : Composite(GenericComposite::class.simpleName!!) {

    @JvmField internal var loadTaskRef = -1
    @JvmField internal var activationTaskRef = -1
    @JvmField internal var deactivationTaskRef = -1
    @JvmField internal var disposeTaskRef = -1

    @JvmField internal val attributes = mutableMapOf<String, String>()
    @JvmField internal val assetRefs = BitSet()
    @JvmField internal val loadedComponents = DynArray.of<CompId>(5, 10)
    @JvmField internal val activatableComponents = DynArray.of<CompId>(5, 10)

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    val withAsset = ComponentRefResolver(Asset) { index -> assetRefs.set(index) }
    fun registerLoadedComponent(id: CompId, activatable: Boolean = false) {
        loadedComponents + id
        if (activatable)
            activatableComponents + id
    }

    val withLoadTask = ComponentRefResolver(ComponentTask) { index -> PlayerSystem.loadTaskRef + index }
    fun <A : ComponentTask> withLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.loadTaskRef = result.instanceId
        return result
    }

    val withActivationTask = ComponentRefResolver(ComponentTask) { index -> PlayerSystem.activationTaskRef + index }
    fun <A : ComponentTask> withActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.activationTaskRef = result.instanceId
        return result
    }

    val withDeactivationTask = ComponentRefResolver(ComponentTask) { index -> PlayerSystem.deactivationTaskRef + index }
    fun <A : ComponentTask> withDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.deactivationTaskRef = result.instanceId
        return result
    }

    val withDisposeTaskRefTask = ComponentRefResolver(ComponentTask) { index -> PlayerSystem.unloadTaskRef + index }
    fun <A : ComponentTask> withDisposeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        PlayerSystem.unloadTaskRef = result.instanceId
        return result
    }

    override fun load() {

        // first load all registered assets
        FFContext.activateAll(Asset, assetRefs)

        // run additional load task if defined
        if (loadTaskRef >= 0)
            TaskSystem.runTask(loadTaskRef, this.componentId)
    }

    override fun activate() {

        // run additional activation task if defined
        if (activationTaskRef >= 0)
            TaskSystem.runTask(activationTaskRef, this.componentId)

        // activate all registered components
        FFContext.activateAll(activatableComponents)
    }

    override fun deactivate() {

        // deactivate all registered components
        FFContext.deactivateAll(activatableComponents)

        // run additional deactivation task if defined
        if (deactivationTaskRef >= 0)
            TaskSystem.runTask(deactivationTaskRef, this.componentId)
    }

    override fun unload() {

        // run additional dispose task if defined
        if (disposeTaskRef >= 0)
            TaskSystem.runTask(disposeTaskRef, this.componentId)

        // clear data
        attributes.clear()
        loadedComponents.clear()
        activatableComponents.clear()
    }

    companion object : SystemComponentSubType<Composite, GenericComposite>(Composite, GenericComposite::class) {
        init { CompositeSystem.compositeBuilderMapping[GenericComposite::class.simpleName!!] = this }
        override fun createEmpty() = GenericComposite()
    }
}