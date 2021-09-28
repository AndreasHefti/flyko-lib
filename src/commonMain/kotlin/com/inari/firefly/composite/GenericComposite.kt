package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.asset.Asset
import com.inari.firefly.control.scene.SceneSystem
import com.inari.firefly.control.task.Task
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.Call
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

open class GenericComposite : Composite(), TriggeredSystemComponent {

    var parentRef = -1
        internal set
    val withParent = ComponentRefResolver(Composite) { index -> parentRef = index }

    @JvmField internal var loadDependsOnParent = true
    @JvmField internal var activationDependsOnParent = false
    @JvmField internal var deactivateAlsoParent = false
    @JvmField internal var disposeAlsoParent = false

    @JvmField internal var loadTaskRef = -1
    @JvmField internal var activationTaskRef = -1
    @JvmField internal var deactivationTaskRef = -1
    @JvmField internal var disposeTaskRef = -1

    @JvmField internal val attributes = mutableMapOf<String, String>()
    @JvmField internal val assetRefs = BitSet()
    @JvmField internal val loadedComponents = DynArray.of<CompId>(5, 10)
    @JvmField internal val activatableComponents = DynArray.of<CompId>(5, 10)

    private val loadCall: Call = { FFContext.load(componentId) }
    private val activateCall: Call = { FFContext.activate(componentId) }
    private val deactivateCall: Call = { FFContext.deactivate(componentId) }
    private val disposeCall: Call = { FFContext.dispose(componentId) }

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    val withAsset = ComponentRefResolver(Asset) { index -> assetRefs.set(index) }
    fun registerLoadedComponent(id: CompId, activatable: Boolean = false) {
        loadedComponents + id
        if (activatable)
            activatableComponents + id
    }

    val withLoadTask = ComponentRefResolver(Task) { index -> loadTaskRef = index }
    fun <A : Task> withLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        loadTaskRef = result.instanceId
        return result
    }

    val withActivationTask = ComponentRefResolver(Task) { index -> activationTaskRef = index }
    fun <A : Task> withActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        activationTaskRef = result.instanceId
        return result
    }

    val withDeactivationTask = ComponentRefResolver(Task) { index -> deactivationTaskRef = index }
    fun <A : Task> withDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        deactivationTaskRef = result.instanceId
        return result
    }

    val withDisposeTask = ComponentRefResolver(Task) { index -> disposeTaskRef = index }
    fun <A : Task> withDisposeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        disposeTaskRef = result.instanceId
        return result
    }

    fun <A : Trigger> withLoadTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = loadCall
        return result
    }

    fun <A : Trigger> withActivationTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = activateCall
        return result
    }

    fun <A : Trigger> withDeactivationTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = deactivateCall
        return result
    }

    fun <A : Trigger> withDisposeTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = disposeCall
        return result
    }

    override fun loadComposite() {

        // if depends on parent and parent is defined load the parent first if not already loaded
        if (loadDependsOnParent && parentRef >= 0)
            FFContext.load(Composite, parentRef)

        // first load all registered assets
        FFContext.activateAll(Asset, assetRefs)

        // run additional load task if defined
        if (loadTaskRef >= 0)
            TaskSystem.runTask(loadTaskRef, this.componentId)
    }

    override fun activateComposite() {

        // if depends on parent and parent is defined activate the parent first if not already active
        if (activationDependsOnParent && parentRef >= 0)
            FFContext.activate(Composite, parentRef)

        // run additional activation task if defined
        if (activationTaskRef >= 0)
            TaskSystem.runTask(activationTaskRef, this.componentId)

        // activate all registered components
        FFContext.activateAll(activatableComponents)
    }

    override fun deactivateComposite() {

        // deactivate all registered components
        FFContext.deactivateAll(activatableComponents)

        // run additional deactivation task if defined
        if (deactivationTaskRef >= 0)
            TaskSystem.runTask(deactivationTaskRef, this.componentId)

        // if depends on parent and parent is defined deactivate the parent also
        if (deactivateAlsoParent && parentRef >= 0)
            FFContext.deactivate(Composite, parentRef)
    }

    override fun disposeComposite() {

        // run additional dispose task if defined
        if (disposeTaskRef >= 0)
            TaskSystem.runTask(disposeTaskRef, this.componentId)

        // clear data
        attributes.clear()
        loadedComponents.clear()
        activatableComponents.clear()

        // if depends on parent and parent is defined dispose the parent also
        if (disposeAlsoParent && parentRef >= 0)
            FFContext.dispose(Composite, parentRef)
    }

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType(): SystemComponentSubType<Composite, out GenericComposite> = Companion
    companion object : SystemComponentSubType<Composite, GenericComposite>(Composite, GenericComposite::class) {
        init { CompositeSystem.compositeBuilderMapping[GenericComposite::class.simpleName!!] = this }
        override fun createEmpty() = GenericComposite()
    }
}