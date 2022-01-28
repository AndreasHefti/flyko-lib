package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.control.Controller
import com.inari.firefly.control.ControllerSystem
import com.inari.firefly.control.task.Task
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.Call
import com.inari.util.StringUtils
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

open class GenericComposite : Composite(), TriggeredSystemComponent {

    /** Task name or pipe '|' separated list of task names or NO_NAME for empty (null) */
    @JvmField var loadTasks = NO_NAME
    /** Task name or pipe '|' separated list of task names or NO_NAME for empty (null) */
    @JvmField var activationTasks = NO_NAME
    /** Task name or pipe '|' separated list of task names or NO_NAME for empty (null) */
    @JvmField var deactivationTasks = NO_NAME
    /** Task name or pipe '|' separated list of task names or NO_NAME for empty (null) */
    @JvmField var disposeTasks = NO_NAME

    @JvmField val attributes = mutableMapOf<String, String>()

    @JvmField internal val assetRefs = BitSet()
    @JvmField internal val loadedComponents = DynArray.of<CompId>(5, 10)
    @JvmField internal val activatableComponents = DynArray.of<CompId>(5, 10)

    private val loadCall: Call = { FFContext.load(componentId) }
    private val activateCall: Call = { FFContext.activate(componentId) }
    private val deactivateCall: Call = { FFContext.deactivate(componentId) }
    private val disposeCall: Call = { FFContext.dispose(componentId) }

    fun getAttribute(name: String): String? = attributes[name]
    fun setAttribute(name: String, value: String) { attributes[name] = value }

    @JvmField val withAsset = ComponentRefResolver(Asset) { index -> assetRefs.set(index) }
    fun <A : Asset> withAsset(builder: SystemComponentSubType<Asset, A>, configure: (A.() -> Unit)): CompId {
        return builder.build(configure)
    }

    fun registerLoadedComponent(id: CompId, activatable: Boolean = false) {
        loadedComponents + id
        if (activatable)
            activatableComponents + id
    }

    @JvmField val withLoadTask = ComponentRefResolver(Task) { index -> loadTasks = appendTaskRef(loadTasks, Task[index]) }
    fun <A : Task> withLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        loadTasks = appendTaskRef(loadTasks, result)
        return result.componentId
    }

    @JvmField val withActivationTask = ComponentRefResolver(Task) { index -> activationTasks = appendTaskRef(activationTasks, Task[index]) }
    fun <A : Task> withActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        activationTasks = appendTaskRef(activationTasks, result)
        return result.componentId
    }

    @JvmField val withDeactivationTask = ComponentRefResolver(Task) { index -> deactivationTasks = appendTaskRef(deactivationTasks, Task[index]) }
    fun <A : Task> withDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        deactivationTasks = appendTaskRef(deactivationTasks, result)
        return result.componentId
    }

    @JvmField val withDisposeTask = ComponentRefResolver(Task) { index -> disposeTasks = appendTaskRef(disposeTasks, Task[index]) }
    fun <A : Task> withDisposeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        disposeTasks = appendTaskRef(disposeTasks, result)
        return result.componentId
    }

    private fun appendTaskRef(taskNames: String, task: Task): String {
        if (task.name == NO_NAME)
            throw IllegalStateException("Task needs a name. Please define a name for this task on creation")
        return if (taskNames != NO_NAME) "$taskNames,${task.name}" else task.name
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

        // first load all registered assets
        FFContext.activateAll(Asset, assetRefs)

        // run additional load tasks if defined
        if (loadTasks != NO_NAME)
            loadTasks
                .split(StringUtils.LIST_VALUE_SEPARATOR)
                .forEach { TaskSystem.runTask(it, this.index) }
    }

    override fun activateComposite() {

        // run additional activation tasks if defined
        if (activationTasks != NO_NAME)
            activationTasks
                .split(StringUtils.LIST_VALUE_SEPARATOR)
                .forEach { TaskSystem.runTask(it, this.index) }

        // activate all registered components
        FFContext.activateAll(activatableComponents)
    }

    override fun deactivateComposite() {

        // deactivate all registered components
        FFContext.deactivateAll(activatableComponents)

        // run additional deactivation task if defined
        if (deactivationTasks != NO_NAME)
            deactivationTasks
                .split(StringUtils.LIST_VALUE_SEPARATOR)
                .forEach { TaskSystem.runTask(it, this.index) }
    }

    override fun disposeComposite() {

        // run additional dispose task if defined
        if (disposeTasks != NO_NAME)
            disposeTasks
                .split(StringUtils.LIST_VALUE_SEPARATOR)
                .forEach { TaskSystem.runTask(it, this.index) }

        // clear data
        attributes.clear()
        loadedComponents.clear()
        activatableComponents.clear()
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