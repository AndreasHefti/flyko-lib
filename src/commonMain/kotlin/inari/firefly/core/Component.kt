package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.COMPONENT_TYPE_ASPECTS
import com.inari.firefly.core.ComponentEventType.*
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.DynArray
import com.inari.util.event.Event

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

enum class ChildLifeCyclePolicy(
    val load: Boolean,
    val activate: Boolean,
    val deactivate: Boolean,
    val dispose: Boolean,
) {
    NONE(false, false, false, false),
    LOAD(true, false, false, true),
    ACTIVATE(true, true, true, true),
}

abstract class ComponentEvent internal constructor(): Event<ComponentEventListener>() {
    internal var index: CompIndex = -1
    internal lateinit var componentEventType: ComponentEventType
    override fun notify(listener: ComponentEventListener) = listener(index, componentEventType)
}

abstract class ComponentType<C : Component>(
    val typeName: String,
) : Aspect {
    val typeAspect: Aspect = COMPONENT_TYPE_ASPECTS.createAspect(typeName)
    override val aspectIndex: Int = typeAspect.aspectIndex
    override val aspectName: String = typeAspect.aspectName
    override val aspectType: AspectType = typeAspect.aspectType
    override fun toString() = "ComponentType:$aspectName"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ComponentType<*>
        if (aspectIndex != other.aspectIndex) return false
        return true
    }
    override fun hashCode(): Int {
        return aspectIndex
    }
}

interface ComponentId {
    val type: ComponentType<*>
    val instanceId: Int
}

open class ComponentKey internal constructor (
    val name: String,
    override val type: ComponentType<*>
) : ComponentId {

    override var instanceId: Int = -1
        internal set

    override fun toString(): String =
        "CKey($name, ${type.aspectName}, $instanceId)"

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

interface SubComponentSystemAdapter<C : Component> {
    operator fun get(index: Int): C
    fun getKey(name: String): ComponentKey
    fun getKey(index: Int) : ComponentKey
    val builder: ComponentBuilder<C>
}

@Suppress("LeakingThis")
@ComponentDSL
abstract class Component protected constructor(
    componentType: ComponentType<*>
) : AbstractIndexed(componentType.typeName), Named {

    override var name: String = NO_NAME
        set(value) {
            if (value === NO_NAME || name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            field = value
            // TODO should it automatically create a key for the name if not existing!?
            val sys = ComponentSystem[componentType]
            if (!sys.hasKey(value))
                sys.createKey(value)
        }

    @JvmField val onInitTask = NO_NAME
    @JvmField val onLoadTask = NO_NAME
    @JvmField val onActivationTask = NO_NAME
    @JvmField val onDeactivationTask = NO_NAME
    @JvmField val onDisposeTask = NO_NAME
    @JvmField val onDeleteTask = NO_NAME

    var initialized: Boolean = false
        protected set
    var loaded: Boolean = false
        protected set
    var active: Boolean = false
        protected set

    internal fun iInitialize() {
        if (initialized)
            return

        initialize()
        this.initialized = true
        if (onInitTask != NO_NAME)
            Task[onInitTask](this.index)
    }

    internal fun iLoad() {
        if (!initialized)
            throw IllegalStateException("Component in illegal state to load")
        if (loaded) {
            loadChildren()
            return
        }

        load()
        this.loaded = true
        if (onLoadTask != NO_NAME)
            Task[onLoadTask](this.index)

        loadChildren()
    }

    private fun loadChildren() {if (children != null)
        children!!.forEach {
            if (it.second.load)
                ComponentSystem.load(it.first)
        }
    }

    internal fun iActivate() {
        if (!initialized)
            throw IllegalStateException("Component in illegal state to activate")
        if (active) {
            activateChildren()
            return
        }

        if (!loaded) // load first before activation
            ComponentSystem[componentType].load(index)

        activate()
        this.active = true
        if (onActivationTask != NO_NAME)
            Task[onActivationTask](this.index)

        activateChildren()
    }

    private fun activateChildren() {
        if (children != null)
            children!!.forEach {
                if (it.second.activate)
                    ComponentSystem.activate(it.first)
            }
    }

    internal fun iDeactivate() {
        if (!initialized)
            throw IllegalStateException("Component in illegal state to activate")

        // deactivate children first
        if (children != null)
            children!!.forEach {
                if (it.second.deactivate)
                    ComponentSystem.deactivate(it.first)
            }

        if (!active)
            return

        if (onDeactivationTask != NO_NAME)
            Task[onDeactivationTask](this.index)
        deactivate()
        this.active = false
    }

    internal fun iDispose() {
        if (!initialized)
            throw IllegalStateException("Component in illegal state to dispose")

        // dispose children first
        if (children != null)
            children!!.forEach {
                if (it.second.dispose)
                    ComponentSystem.dispose(it.first)
            }

        if (!loaded)
            return
        if (active) // deactivate first if still active
            ComponentSystem[componentType].deactivate(index)

        if (onDisposeTask != NO_NAME)
            Task[onDisposeTask](this.index)
        dispose()
        this.loaded = false
    }

    internal fun iDelete() {
        if (!initialized) {
            children?.clear()
            disposeIndex()
            return
        }

        if (loaded) // dispose first when still loaded
            ComponentSystem[componentType].dispose(index)

        // delete children first
        if (children != null)
            children!!.forEach {
                ComponentSystem.delete(it.first)
            }

        if (onDeleteTask != NO_NAME)
            Task[onDeleteTask](this.index)

        delete()

        this.initialized = false
        children?.clear()
        disposeIndex()
    }

    protected open fun initialize() {}
    protected open fun load() {}
    protected open fun activate() {}
    protected open fun deactivate() {}
    protected open fun dispose() {}
    protected open fun delete() {}

    // **** Child References ****
    // **************************
    private var children: DynArray<Pair<ComponentKey, ChildLifeCyclePolicy>>? = null

    fun withChild(key: ComponentKey, policy: ChildLifeCyclePolicy = ChildLifeCyclePolicy.LOAD): ComponentKey {
        if (children == null)
            children = DynArray.of()
        children!!.add(Pair(key, policy))
        return key
    }

    fun <C : Component> withChild(
        type: ComponentType<C>,
        policy: ChildLifeCyclePolicy = ChildLifeCyclePolicy.LOAD,
        configure: (C.() -> Unit)): ComponentKey = withChild(ComponentSystem[type].builder, policy, configure)

    fun <C : Component> withChild(
        cBuilder: ComponentBuilder<C>,
        policy: ChildLifeCyclePolicy = ChildLifeCyclePolicy.LOAD,
        configure: (C.() -> Unit)): ComponentKey {

        if (children == null)
            children = DynArray.of()

        val child = cBuilder.buildAndGet(configure)

        if (child.name == NO_NAME)
            throw IllegalArgumentException("Component needs a name if it is uses as child reference")

        child.notifyParent(this)
        val key = ComponentSystem[child.componentType].getKey(child.name)
        children!!.add(Pair(key, policy))
        return  key
    }

    fun removeChild(key: ComponentKey) =
        children?.find { it.first == key }?.let { pair -> children?.remove(pair) }

    protected open fun notifyParent(comp: Component) {}
    protected fun <T> checkNotLoaded(value: T, s: String): T {
        if (loaded)
            throw IllegalStateException("No property change allows on load: $s")
        return value
    }

    override fun toString(): String =
        "Comp($index, ${componentType.aspectName}, $name, l:$loaded, a:$active)"

    open val componentGroupType: ComponentType<out Component>? = null
    abstract val componentType: ComponentType<out Component>
    companion object {
        @JvmField internal val COMPONENT_TYPE_ASPECTS = IndexedAspectType("COMPONENT_ASPECTS")
        @JvmField internal val NO_COMP_TYPE = object : ComponentType<Component>("NO_COMP_TYPE") {}
        @JvmField val NO_COMPONENT_KEY = ComponentKey(NO_NAME, NO_COMP_TYPE)
    }
}

abstract class ComponentBuilder<C : Component>(
    internal val system: ComponentSystem<in C>
) {

    fun build(configure: C.() -> Unit): ComponentKey {
        @Suppress("UNCHECKED_CAST")
        val comp: C = create()
        comp.also(configure)
        val key = system.registerComponent(comp)
        comp.iInitialize()
        system.send(comp.index, INITIALIZED)
        return key
    }

    fun buildActive(configure: C.() -> Unit): ComponentKey {
        val key = build(configure)
        system.activate(key)
        return key
    }

    fun buildAndGet(configure: C.() -> Unit): C {
        @Suppress("UNCHECKED_CAST")
        val comp: C = create()
        comp.also(configure)
        system.registerComponent(comp)
        comp.iInitialize()
        system.send(comp.index, INITIALIZED)
        return comp
    }

    fun buildAndGetActive(configure: C.() -> Unit): C {
        val c = buildAndGet(configure)
        system.activate(c.index)
        return c
    }

    protected abstract fun create(): C
}

