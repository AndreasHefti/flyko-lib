package com.inari.firefly.core

import com.inari.firefly.core.ComponentEventType.*
import com.inari.firefly.core.Apply.*
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.DynArray

/** Defines the component based builder DSL marker */
@DslMarker annotation class ComponentDSL
/** Component event listener expects the component index, component type and the component event type */
typealias CompIndex = Int
typealias ComponentEventListener = (CompIndex, ComponentEventType) -> Unit

enum class ComponentEventType {
    INITIALIZED,
    LOADED,
    ACTIVATED,
    DEACTIVATED,
    DISPOSED,
    DELETED
}

enum class Apply {
    NONE,
    BEFORE,
    AFTER
}

interface ApplyPolicy {
    val parentPolicy: Apply
    val childPolicy: Apply
}

enum class ApplyPolicies(
    override val parentPolicy: Apply,
    override val childPolicy: Apply) : ApplyPolicy {

    DEFAULT_LOAD(BEFORE, AFTER),
    DEFAULT_ACTIVATE(BEFORE, AFTER),
    DEFAULT_DEACTIVATE(NONE, BEFORE),
    DEFAULT_DISPOSE(NONE, BEFORE),
    DEFAULT_DELETE(NONE, BEFORE)
}

interface ComponentType<C : Component> : Aspect {
    val typeName: String
    val subTypeName: String
}

interface ComponentId {
    val type: ComponentType<*>
    val instanceIndex: Int
}

open class ComponentKey internal constructor (
    val name: String,
    override val type: ComponentType<*>
) : ComponentId {

    override var instanceIndex: Int = -1
        internal set

    override fun toString(): String =
        "CKey($name, ${type.aspectName}, $instanceIndex)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComponentKey
        if (name != other.name) return false
        if (type.aspectName != other.type.aspectName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.aspectName.hashCode()
        return result
    }
}

@ComponentDSL
abstract class Component protected constructor(
    val componentType: ComponentType<out Component>
) : AbstractIndexed(componentType.typeName), Named {

    @JvmField var autoActivation = false
    var key = NO_COMPONENT_KEY
        private set
    override var name: String = NO_NAME
        set(value) {
            if (value === NO_NAME || name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            field = value
            // TODO should it automatically create a key for the name if not existing!?
            key = ComponentSystem[componentType].getKey(name, true)
        }

    @JvmField internal var onStateChange = false
    var initialized: Boolean = false
        internal set
    var loaded: Boolean = false
        internal set
    var active: Boolean = false
        internal set

    internal fun iInitialize() = initialize()
    internal fun iLoad() = load()
    internal fun iActivate() = activate()
    internal fun iDeactivate() = deactivate()
    internal fun iDispose() = dispose()
    internal fun iDelete() = disposeIndex()

    protected open fun initialize() {}
    protected open fun load() {}
    protected open fun activate() {}
    protected open fun deactivate() {}
    protected open fun dispose() {}
    protected open fun delete() {}

    internal open fun stateChangeProcessing(apply: Apply, type: ComponentEventType) {}

    protected fun <T> checkNotLoaded(value: T, s: String): T {
        if (loaded)
            throw IllegalStateException("No property change allows on load: $s")
        return value
    }

    override fun toString(): String =
        "${componentType.aspectName}($index, $name, l:$loaded, a:$active)"

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

abstract class ComponentNode protected constructor(componentType: ComponentType<*>) : Component(componentType) {

    @JvmField val onInitTask = CLooseReference(Task)
    @JvmField val onLoadTask = CLooseReference(Task)
    @JvmField val onActivationTask = CLooseReference(Task)
    @JvmField val onDeactivationTask = CLooseReference(Task)
    @JvmField val onDisposeTask = CLooseReference(Task)
    @JvmField val onDeleteTask = CLooseReference(Task)

    @JvmField var loadPolicy = ApplyPolicies.DEFAULT_LOAD
    @JvmField var activationPolicy = ApplyPolicies.DEFAULT_ACTIVATE
    @JvmField var deactivationPolicy = ApplyPolicies.DEFAULT_DEACTIVATE
    @JvmField var disposePolicy = ApplyPolicies.DEFAULT_DISPOSE
    @JvmField var deletePolicy = ApplyPolicies.DEFAULT_DELETE

    override fun initialize() {
        if (onInitTask.exists)
            Task[onInitTask](this.index)
    }

    override fun load() {
        if (onLoadTask.exists)
            Task[onLoadTask](this.index)
    }

    override fun activate() {
        if (onActivationTask.exists)
            Task[onActivationTask](this.index)
    }

    override fun deactivate() {
        if (onDeactivationTask.exists)
            Task[onDeactivationTask](this.index)
    }

    override fun dispose() {
        if (onDisposeTask.exists)
            Task[onDisposeTask](this.index)
    }

    override fun delete() {
        if (onDeleteTask.exists)
            Task[onDeleteTask](this.index)
    }

    protected var parent: ComponentKey = NO_COMPONENT_KEY
    protected var children: DynArray<ComponentKey>? = null

    protected open fun setParentComponent(key: ComponentKey) {
        parent = key
    }
    fun withChild(key: ComponentKey): ComponentKey {
        val child: ComponentNode = ComponentSystem[key]
        if (child.parent != NO_COMPONENT_KEY)
            throw IllegalArgumentException("ComponentNode for key: $key has already a parent")
        else
            child.setParentComponent(ComponentSystem[this.componentType].getKey(this.index))

        if (children == null)
            children = DynArray.of()
        children!!.add(key)

        return key
    }

    fun <C : ComponentNode> withChild(
        cBuilder: ComponentBuilder<C>,
        configure: (C.() -> Unit)): ComponentKey {

        if (this.name == NO_NAME)
            throw IllegalArgumentException("Component needs a name if it is used as a parent reference")

        if (children == null)
            children = DynArray.of()

        val child = cBuilder.buildAndGet(configure)
        if (child.name == NO_NAME)
            throw IllegalArgumentException("Component needs a name if it is used as child reference")

        child.setParentComponent(ComponentSystem[this.componentType].getKey(this.name))
        val key = ComponentSystem[child.componentType].getKey(child.name)
        children!!.add(key)
        return  key
    }

    fun removeChild(key: ComponentKey) {
        if (children == null || key !in children!!) return
        children?.remove(key)
        val child: ComponentNode = ComponentSystem[key]
        child.setParentComponent(NO_COMPONENT_KEY)
    }

    override fun stateChangeProcessing(apply: Apply, type: ComponentEventType) {
        val policy: ApplyPolicies
        val process: (ComponentKey) -> Unit
        when (type) {
            LOADED -> {
                policy = loadPolicy
                process = ComponentSystem.Companion::load
            }
            ACTIVATED -> {
                policy = activationPolicy
                process = ComponentSystem.Companion::activate
            }
            DEACTIVATED -> {
                policy = deactivationPolicy
                process = ComponentSystem.Companion::deactivate
            }
            DISPOSED -> {
                policy = disposePolicy
                process = ComponentSystem.Companion::dispose
            }
            DELETED -> {
                policy = deletePolicy
                process = ComponentSystem.Companion::delete
            }
            else -> return
        }

        if (apply == BEFORE && policy.childPolicy == BEFORE)
            children?.forEach(process)
        if (apply == BEFORE && parent != NO_COMPONENT_KEY && policy.parentPolicy == BEFORE)
            process(parent)
        if (apply == AFTER && policy.childPolicy == AFTER)
            children?.forEach(process)
        if (apply == AFTER && parent != NO_COMPONENT_KEY && policy.parentPolicy == AFTER)
            process(parent)
    }
}

interface ComponentBuilder<C : Component> {
    operator fun invoke(configure: C.() -> Unit): ComponentKey = build(configure)
    fun build(configure: C.() -> Unit): ComponentKey
    fun buildAndGet(configure: C.() -> Unit): C
}


