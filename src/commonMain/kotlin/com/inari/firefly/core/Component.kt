package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.COMPONENT_TYPE_ASPECTS
import com.inari.firefly.core.ComponentEventType.*
import com.inari.firefly.core.Apply.*
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

//enum class ChildLifeCyclePolicy(
//    val load: Boolean,
//    val activate: Boolean,
//    val deactivate: Boolean,
//    val dispose: Boolean,
//) {
//    NONE(false, false, false, false),
//    LOAD(true, false, false, true),
//    ACTIVATE(true, true, true, true),
//}

enum class Apply { NONE, BEFORE, AFTER }
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

    @JvmField var onInitTask = NO_NAME
    @JvmField var onLoadTask = NO_NAME
    @JvmField var onActivationTask = NO_NAME
    @JvmField var onDeactivationTask = NO_NAME
    @JvmField var onDisposeTask = NO_NAME
    @JvmField var onDeleteTask = NO_NAME

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

    internal fun iLoad(apply: ApplyPolicy) {
        this.loaded = true

        if (apply.childPolicy == BEFORE)
            processChildren(LOADED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == BEFORE)
            ComponentSystem.load(parent)

        load()

        if (onLoadTask != NO_NAME)
            Task[onLoadTask](this.index)

        if (apply.childPolicy == AFTER)
            processChildren(LOADED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == AFTER)
            ComponentSystem.load(parent)
    }

    internal fun iActivate(apply: ApplyPolicy) {
        this.active = true

        if (!loaded) // load first before activation
            ComponentSystem[componentType].load(index)
        if (apply.childPolicy == BEFORE)
            processChildren(ACTIVATED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == BEFORE)
            ComponentSystem.activate(parent)

        activate()

        if (onActivationTask != NO_NAME)
            Task[onActivationTask](this.index)

        if (apply.childPolicy == AFTER)
            processChildren(ACTIVATED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == AFTER)
            ComponentSystem.activate(parent)
    }

    internal fun iDeactivate(apply: ApplyPolicy) {
        this.active = false

        if (apply.childPolicy == BEFORE)
            processChildren(DEACTIVATED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == BEFORE)
            ComponentSystem.delete(parent)

        if (onDeactivationTask != NO_NAME)
            Task[onDeactivationTask](this.index)

        deactivate()

        if (apply.childPolicy == AFTER)
            processChildren(DEACTIVATED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == AFTER)
            ComponentSystem.deactivate(parent)
    }

    internal fun iDispose(apply: ApplyPolicy) {
        this.loaded = false

        if (active) // deactivate first if still active
            ComponentSystem[componentType].deactivate(index)
        if (apply.childPolicy == BEFORE)
            processChildren(DISPOSED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == BEFORE)
            ComponentSystem.dispose(parent)

        if (onDisposeTask != NO_NAME)
            Task[onDisposeTask](this.index)
        dispose()

        if (apply.childPolicy == AFTER)
            processChildren(DISPOSED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == AFTER)
            ComponentSystem.dispose(parent)
    }

    internal fun iDelete(apply: ApplyPolicy) {
        if (!initialized) {
            children?.clear()
            disposeIndex()
            return
        }

        if (loaded) // dispose first when still loaded
            ComponentSystem[componentType].dispose(index)
        if (apply.childPolicy == BEFORE)
            processChildren(DELETED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == BEFORE)
            ComponentSystem.delete(  parent)

        if (onDeleteTask != NO_NAME)
            Task[onDeleteTask](this.index)
        delete()
        this.initialized = false

        if (apply.childPolicy == AFTER)
            processChildren(DELETED)
        if (parent != NO_COMPONENT_KEY && apply.parentPolicy == AFTER)
            ComponentSystem.delete(parent)

        children?.clear()
        disposeIndex()
    }

    private fun processChildren(type: ComponentEventType) {
        children?.forEach {
            when (type) {
                LOADED -> ComponentSystem.load(it)
                ACTIVATED -> ComponentSystem.activate(it)
                DEACTIVATED -> ComponentSystem.deactivate(it)
                DISPOSED -> ComponentSystem.dispose(it)
                DELETED -> ComponentSystem.delete(it)
                else -> {}
            }
        }
    }

    protected open fun initialize() {}
    protected open fun load() {}
    protected open fun activate() {}
    protected open fun deactivate() {}
    protected open fun dispose() {}
    protected open fun delete() {}

    // **** References ****
    // ********************
    protected var parent: ComponentKey = NO_COMPONENT_KEY
    protected var children: DynArray<ComponentKey>? = null

    protected open fun setParentComponent(key: ComponentKey) {
        parent = key
    }
    fun withChild(key: ComponentKey): ComponentKey {
        val child: Component = ComponentSystem[key]
        if (child.parent != NO_COMPONENT_KEY)
            throw IllegalArgumentException("Component for key: $key has already a parent")
        else
            child.setParentComponent(ComponentSystem[this.componentType].getKey(this.index))

        if (children == null)
            children = DynArray.of()
        children!!.add(key)

        return key
    }

    fun <C : Component> withChild(
        type: ComponentType<C>,
        configure: (C.() -> Unit)): ComponentKey = withChild(ComponentSystem[type].builder, configure)

    fun <C : Component> withChild(
        cBuilder: ComponentBuilder<C>,
        configure: (C.() -> Unit)): ComponentKey {

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
        val child: Component = ComponentSystem[key]
        child.setParentComponent(NO_COMPONENT_KEY)
    }

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

    init {
        @Suppress("LeakingThis")
        ComponentSystem.COMPONENT_BUILDER_MAPPING[system.aspectName] = this
    }

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

