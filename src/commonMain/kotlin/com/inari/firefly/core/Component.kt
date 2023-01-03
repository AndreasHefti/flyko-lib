package com.inari.firefly.core

import com.inari.firefly.core.ComponentEventType.*
import com.inari.firefly.core.Apply.*
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.firefly.graphics.view.ViewLayerAware
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.jvm.JvmField
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.geom.Vector2f

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
    DEFAULT_DELETE(NONE, BEFORE),

    PARENT_BEFORE(BEFORE, NONE)
}

interface ComponentType<C : Component> : Aspect {
    val typeName: String
    val subTypeName: String
}

sealed interface ComponentId {
    val type: ComponentType<*>
    val instanceIndex: Int
}

class ComponentKey internal constructor (
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
    override var name: String = NO_NAME
        set(value) {
            if (value === NO_NAME || name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            field = value
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
    internal fun iDelete() = delete()
    internal fun iDisposeIndex() = disposeIndex()

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

abstract class ComponentNode protected constructor(componentType: ComponentType<*>) : Component(componentType) {

    @JvmField var loadPolicy = ApplyPolicies.DEFAULT_LOAD
    @JvmField var activationPolicy = ApplyPolicies.DEFAULT_ACTIVATE
    @JvmField var deactivationPolicy = ApplyPolicies.DEFAULT_DEACTIVATE
    @JvmField var disposePolicy = ApplyPolicies.DEFAULT_DISPOSE
    @JvmField var deletePolicy = ApplyPolicies.DEFAULT_DELETE

    var parent: ComponentKey = NO_COMPONENT_KEY
        protected set

    val children: DynArrayRO<ComponentKey>?
        get() = writableChildren

    protected var writableChildren : DynArray<ComponentKey>? = null

    protected open fun setParentComponent(key: ComponentKey) {
        parent = key
    }
    fun withChild(key: ComponentKey): ComponentKey {
        if (children == null)
            writableChildren = DynArray.of(5, 5)

        writableChildren!!.add(key)
        return key
    }

    fun <C : ComponentNode> withChild(
        cBuilder: ComponentBuilder<C>,
        configure: (C.() -> Unit)): ComponentKey {
        if (children == null)
            writableChildren = DynArray.of()

        val child = cBuilder.buildAndGet(configure)
        val key = ComponentSystem[child.componentType].getKey(child.name)
        writableChildren!!.add(key)
        return  key
    }

    override fun initialize() {
        super.initialize()
        children?.forEach {
            ComponentSystem.get<ComponentNode>(it).setParentComponent(this.key)
        }
    }

    fun removeChild(key: ComponentKey) {
        if (children == null || key !in children!!) return
        writableChildren?.remove(key)
        val child: ComponentNode = ComponentSystem[key]
        child.setParentComponent(NO_COMPONENT_KEY)
    }

    private fun updateReferences() {
        if (parent != NO_COMPONENT_KEY && parent.instanceIndex < 0)
            parent = NO_COMPONENT_KEY
        children?.forEach {
            if (it.instanceIndex < 0)
                writableChildren?.remove(it)
        }
        writableChildren?.trim()
    }

    override fun stateChangeProcessing(apply: Apply, type: ComponentEventType) {
        updateReferences()
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

open class Composite protected constructor(
    subType: ComponentType<out Composite>
) : ComponentNode(subType), ViewLayerAware {

    @JvmField val onInitTask = CLooseReference(Task)
    @JvmField val onLoadTask = CLooseReference(Task)
    @JvmField val onActivationTask = CLooseReference(Task)
    @JvmField val onDeactivationTask = CLooseReference(Task)
    @JvmField val onDisposeTask = CLooseReference(Task)
    @JvmField val onDeleteTask = CLooseReference(Task)

    fun withInitTask(configure: (Task.() -> Unit)) = onInitTask.setKey(Task(configure))
    fun withLoadTask(configure: (Task.() -> Unit)) = onLoadTask.setKey(Task(configure))
    fun withActivationTask(configure: (Task.() -> Unit)) = onActivationTask.setKey(Task(configure))
    fun withDeactivationTask(configure: (Task.() -> Unit)) = onDeactivationTask.setKey(Task(configure))
    fun withDisposeTask(configure: (Task.() -> Unit)) = onDisposeTask.setKey(Task(configure))
    fun withDeleteTask(configure: (Task.() -> Unit)) = onDeleteTask.setKey(Task(configure))

    override val viewIndex: Int
        get() = viewRef.targetKey.instanceIndex
    override val layerIndex: Int
        get() = layerRef.targetKey.instanceIndex

    @JvmField var viewRef = CReference(View)
    @JvmField var layerRef = CReference(Layer)
    @JvmField val position = Vector2f()

    @JvmField internal val attributes = mutableMapOf<String, String>()

    fun setAttribute(name: String, value: String) { attributes[name] = value }
    fun getAttribute(name: String): String? = attributes[name]
    fun getAttributeFloat(name: String): Float? = attributes[name]?.toFloat()

    override fun initialize() {
        super.initialize()
        if (onInitTask.exists)
            Task[onInitTask](this.index)
    }

    override fun load() {
        super.load()
        if (onLoadTask.exists)
            Task[onLoadTask](this.index)
    }

    override fun activate() {
        super.activate()
        if (onActivationTask.exists)
            Task[onActivationTask](this.index)
    }

    override fun deactivate() {
        if (onDeactivationTask.exists)
            Task[onDeactivationTask](this.index)
        super.deactivate()
    }

    override fun dispose() {
        if (onDisposeTask.exists)
            Task[onDisposeTask](this.index)
        super.dispose()
    }

    override fun delete() {
        if (onDeleteTask.exists)
            Task[onDeleteTask](this.index)
        super.delete()
    }

    companion object : ComponentSystem<Composite>("Composite") {
        override fun allocateArray(size: Int): Array<Composite?> = arrayOfNulls(size)
        override fun create(): Composite =
            throw UnsupportedOperationException("Composite is abstract use a concrete implementation instead")
    }
}




