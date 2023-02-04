package com.inari.firefly.core

import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.EMPTY_DICTIONARY

/** Defines the component based builder DSL marker */
@DslMarker annotation class ComponentDSL
/** Component event listener expects the component index, component type and the component event type */
typealias ComponentEventListener = (key: ComponentKey, ComponentEventType) -> Unit

enum class ComponentEventType {
    INITIALIZED,
    LOADED,
    ACTIVATED,
    DEACTIVATED,
    DISPOSED,
    DELETED
}

enum class LifecycleTaskType {
    AFTER_INIT,
    BEFORE_LOAD,
    AFTER_LOAD,
    BEFORE_ACTIVATION,
    AFTER_ACTIVATION,
    BEFORE_DEACTIVATION,
    AFTER_DEACTIVATION,
    BEFORE_DISPOSE,
    AFTER_DISPOSE,
    BEFORE_DELETE,
    AFTER_DELETE
}

interface ComponentType<C : Component> : Aspect {
    val typeName: String
    val subTypeName: String
}

sealed interface ComponentId {
    val type: ComponentType<*>
    val componentIndex: ComponentIndex
}

class ComponentKey internal constructor (
    name: String,
    override val type: ComponentType<*>
) : ComponentId {

    init {
        // DEBUG  println("-> new ComponentKey: $name : ${type.typeName} : ${type.subTypeName}")
    }

    internal constructor(index: ComponentIndex, type: ComponentType<*>) : this(NO_NAME, type) {
        componentIndex = index
    }

    var name: String = name
        private set
    override var componentIndex: ComponentIndex = NULL_COMPONENT_INDEX
        internal set

    override fun toString(): String =
        "CKey($name, ${type.aspectName}, $componentIndex)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComponentKey
        if (name != other.name) return false
        if (type.aspectName != other.type.aspectName) return false
        return true
    }

    internal fun clearKey() {
        // DEBUG  println("-> clear ComponentKey: $name $type")
        name = NO_NAME
        componentIndex = NULL_COMPONENT_INDEX
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
    }
}

open class Composite protected constructor(
    subType: ComponentType<out Composite>
) : Component(subType) {

    private val tasks: Array<ComponentKey?> = arrayOfNulls(LifecycleTaskType.values().size)

    @JvmField var attributes = EMPTY_DICTIONARY

    fun withTask(apply: LifecycleTaskType, taskKey: ComponentKey) {
        if (taskKey.type != Task) throw IllegalArgumentException("Key mismatch taskKey is not of expected type Task")
        tasks[apply.ordinal] = taskKey
    }
    fun withTask(apply: LifecycleTaskType, name: String) {
        tasks[apply.ordinal] = Task.getOrCreateKey(name)
    }
    fun withTask(apply: LifecycleTaskType, configure: (Task.() -> Unit)) {
        tasks[apply.ordinal] = Task(configure)
    }

    override fun iInitialize() {
        super.iInitialize()
        runTaskIfDefined(LifecycleTaskType.AFTER_INIT)
    }

    override fun iLoad() {
        runTaskIfDefined(LifecycleTaskType.BEFORE_LOAD)
        super.iLoad()
        runTaskIfDefined(LifecycleTaskType.AFTER_LOAD)
    }

    override fun iActivate() {
        runTaskIfDefined(LifecycleTaskType.BEFORE_ACTIVATION)
        super.iActivate()
        runTaskIfDefined(LifecycleTaskType.AFTER_ACTIVATION)
    }

    override fun iDeactivate() {
        runTaskIfDefined(LifecycleTaskType.BEFORE_DEACTIVATION)
        super.iDeactivate()
        runTaskIfDefined(LifecycleTaskType.AFTER_DEACTIVATION)
    }

    override fun iDispose() {
        runTaskIfDefined(LifecycleTaskType.BEFORE_DISPOSE)
        super.iDispose()
        runTaskIfDefined(LifecycleTaskType.AFTER_DISPOSE)
    }

    override fun iDelete() {
        runTaskIfDefined(LifecycleTaskType.BEFORE_DELETE)
        super.iDelete()
        runTaskIfDefined(LifecycleTaskType.AFTER_DELETE)
    }

    private fun runTaskIfDefined(type: LifecycleTaskType) =
        tasks[type.ordinal]?.apply {
            if (this.componentIndex >= 0)
                Task[this](this@Composite.index, attributes)
        }

    companion object : ComponentSystem<Composite>("Composite") {
        override fun allocateArray(size: Int): Array<Composite?> = arrayOfNulls(size)
        override fun create(): Composite =
            throw UnsupportedOperationException("Composite is abstract use a concrete implementation instead")
    }
}