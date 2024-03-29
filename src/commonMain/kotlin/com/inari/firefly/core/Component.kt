package com.inari.firefly.core

import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.collection.AttributesRO.Companion.EMPTY_ATTRIBUTES
import com.inari.util.collection.DynArray
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField

/** Defines the component based builder DSL marker */
@DslMarker annotation class ComponentDSL
/** Component event listener expects the component index, component type and the component event type */
typealias ComponentEventListener = (ComponentKey, ComponentEventType) -> Unit

enum class ComponentEventType {
    INITIALIZED,
    LOADED,
    ACTIVATED,
    DEACTIVATED,
    DISPOSED,
    DELETED
}

interface ComponentType<C : Component> : Aspect {
    val typeName: String
    val subTypeName: String
}

class ComponentKey internal constructor (
    @JvmField val name: String,
    @JvmField val type: ComponentType<*>
) {

    internal constructor(index: ComponentIndex, type: ComponentType<*>) : this(NO_NAME, type) {
        componentIndex = index
    }

    @JvmField var componentIndex: ComponentIndex = NULL_COMPONENT_INDEX
        //internal set

    override fun toString(): String =
        "CKey($name, ${type.aspectName}, $componentIndex)"

    internal fun clearKey() {
        componentIndex = NULL_COMPONENT_INDEX
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComponentKey
        if (type.aspectIndex != other.type.aspectIndex) return false
        if (name.hashCode() != other.name.hashCode()) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.aspectName.hashCode()
        return result
    }
}

sealed interface ComponentBuilder<C : Component> {
    operator fun invoke(configure: C.() -> Unit): ComponentKey = build(configure)
    fun build(configure: C.() -> Unit): ComponentKey
    fun buildAndGet(configure: C.() -> Unit): C
}

@ComponentDSL
abstract class Component protected constructor(
    val componentType: ComponentType<out Component>
) : AbstractIndexed(componentType.typeName), Named {

    @JvmField var survivesSystemClear = false
    @JvmField var autoLoad = false
    @JvmField var autoActivation = false

    var key = NO_COMPONENT_KEY
        internal set
    fun earlyKeyAccess(): ComponentKey {
        // DEBUG  println("--> earlyKeyAccess: $this")
        if (key != NO_COMPONENT_KEY) return key
        return if (name != NO_NAME) {
            key = ComponentSystem[componentType].getOrCreateKey(name)
            key.componentIndex = this.index
            key
        }
        else
            ComponentKey(this.index, componentType)
    }

    override var name: String = NO_NAME
        set(value) {
            if (value === NO_NAME || name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            if (this.key != NO_COMPONENT_KEY && this.key.name != value)
                throw IllegalStateException("An illegal reassignment of name, key already set: $value to: $name key: $key")
            field = value
        }

    @JvmField val groups = COMPONENT_GROUP_ASPECT.createAspects()
    open fun addToGroup(group: Aspect) = groups + group
    open fun removeFromGroup(group: Aspect) = groups - group

    @JvmField internal var onStateChange = false
    var initialized: Boolean = false
        internal set
    var loaded: Boolean = false
        internal set
    var active: Boolean = false
        internal set

    internal open fun iInitialize() = initialize()
    internal open fun iLoad() = load()
    internal open fun iActivate() = activate()
    internal open fun iDeactivate() = deactivate()
    internal open fun iDispose() = dispose()
    internal open fun iDelete() = delete()
    internal open fun iDisposeIndex() = disposeIndex()

    protected open fun initialize() {}
    protected open fun load() {}
    protected open fun activate() {}
    protected open fun deactivate() {}
    protected open fun dispose() {}
    protected open fun delete() {}

    protected fun <T> checkNotLoaded(value: T, s: String): T {
        if (loaded)
            throw IllegalStateException("No property change allows on load: $s")
        return value
    }

    override fun toString(): String =
        "${componentType.aspectName}${ 
            if(componentType.aspectName != componentType.subTypeName) 
                ":${componentType.subTypeName}" 
            else "" }" +
                "($index, $name, l:$loaded, a:$active)"

    companion object {
        @JvmField internal val COMPONENT_TYPE_ASPECTS = IndexedAspectType("COMPONENT_ASPECTS")
        @JvmField internal val NO_COMP_TYPE = object : ComponentType<Component> {
            override val typeName = "NO_COMP_TYPE"
            override val subTypeName = typeName
            override val aspectName = typeName
            override val aspectType = COMPONENT_TYPE_ASPECTS
            override val aspectIndex = -1
        }
        @JvmField val NO_COMPONENT_KEY = ComponentKey(NO_NAME, NO_COMP_TYPE)
        @JvmField val COMPONENT_GROUP_ASPECT = IndexedAspectType("COMPONENT_GROUP_ASPECT")
    }
}

open class AttributedComponent protected constructor(
    subType: ComponentType<out Composite>
) : Component(subType) {

    @JvmField var attributes = EMPTY_ATTRIBUTES

    companion object : ComponentSystem<Composite>("AttributedComponent") {
        override fun allocateArray(size: Int): Array<Composite?> = arrayOfNulls(size)
        override fun create(): Composite =
            throw UnsupportedOperationException("AttributedComponent is abstract use a concrete implementation instead")
    }
}

enum class LifecycleTaskType {
    ON_LOAD,
    ON_ACTIVATION,
    ON_DEACTIVATION,
    ON_DISPOSE,
    ON_DELETE
}

@ComponentDSL
class LifecycleTask {
    @JvmField var order = 1
    @JvmField var attributes = EMPTY_ATTRIBUTES
    @JvmField var lifecycleType = LifecycleTaskType.ON_LOAD
    @JvmField val task = CReference(Task)
}

open class Composite protected constructor(
    subType: ComponentType<out Composite>
) : AttributedComponent(subType) {

    private val tasks = DynArray.of<LifecycleTask>(2, 5)
    private val lifecycleComponents = DynArray.of<ComponentKey>(2, 5)

    fun withLifecycleComponent(key: ComponentKey) =
        lifecycleComponents + key

    fun withLifecycleTask(lifecycleTask: LifecycleTask) {
        tasks + lifecycleTask
        sortTasks()
    }

    fun withLifecycleTask(configure: (LifecycleTask.() -> Unit)): LifecycleTask {
        val lifecycleTask = LifecycleTask()
        configure(lifecycleTask)
        tasks + lifecycleTask
        sortTasks()
        return lifecycleTask
    }

    override fun load() {
        super.load()
        runTaskIfDefined(LifecycleTaskType.ON_LOAD)
        val iter = lifecycleComponents.iterator()
        while (iter.hasNext())
            ComponentSystem.load(iter.next())
    }

    override fun activate() {
        super.activate()
        runTaskIfDefined(LifecycleTaskType.ON_ACTIVATION)
        val iter = lifecycleComponents.iterator()
        while (iter.hasNext())
            ComponentSystem.activate(iter.next())
    }

    override fun deactivate() {
        val iter = lifecycleComponents.iterator()
        while (iter.hasNext())
            ComponentSystem.deactivate(iter.next())
        runTaskIfDefined(LifecycleTaskType.ON_DEACTIVATION)
        super.deactivate()
    }

    override fun dispose() {
        val iter = lifecycleComponents.iterator()
        while (iter.hasNext())
            ComponentSystem.dispose(iter.next())
        runTaskIfDefined(LifecycleTaskType.ON_DISPOSE)
        super.dispose()
    }

    override fun delete() {
        val iter = lifecycleComponents.iterator()
        while (iter.hasNext())
            ComponentSystem.delete(iter.next())
        runTaskIfDefined(LifecycleTaskType.ON_DELETE)
        super.delete()
    }

    private fun runTaskIfDefined(type: LifecycleTaskType) {
        val coIter = tasks.iterator()
        while (coIter.hasNext()) {
            val co = coIter.next()
            if (co.lifecycleType == type)
                if (!co.task.exists)
                    println("**** WARN: Task ${co.task} does not exist")
                else
                    Task[co.task](this@Composite.key, co.attributes)
        }
    }

    private fun sortTasks() {
        tasks.sort { lt1, lt2 ->
            lt1?.order?.compareTo(lt2?.order ?: 0)  ?: -1
        }
    }

    companion object : ComponentSystem<Composite>("Composite") {
        override fun allocateArray(size: Int): Array<Composite?> = arrayOfNulls(size)
        override fun create(): Composite =
            throw UnsupportedOperationException("Composite is abstract use a concrete implementation instead")
    }
}