package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.NO_NAME
import com.inari.util.TRUE_PREDICATE
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.event.Event

interface System {
    fun clearSystem()
}

interface IComponentSystem<C : Component> : ComponentBuilder<C>, ComponentType<C>, System {
    val systemName: String
    val hasComponents: Boolean
    val hasActiveComponents: Boolean

    fun getNextIndex(fromIndex: Int): Int
    fun getNextActiveIndex(fromIndex: Int): Int

    fun createKey(name: String): ComponentKey
    fun hasKey(name: String): Boolean
    fun getOrCreateKey(name: String): ComponentKey
    /** Returns NO_COMPONENT_KEY if key doesn't exist */
    fun getKey(name: String): ComponentKey
    /** Returns NO_COMPONENT_KEY if key doesn't exist */
    fun getKey(index: Int): ComponentKey

    operator fun get(name: String): C {
        val keyForName = getKey(name)
        if (keyForName == NO_COMPONENT_KEY) throw IllegalArgumentException("No component for name: $name found on system: $systemName")
        checkKey(keyForName)
        return get(keyForName.instanceIndex)
    }
    operator fun get(ref: CLooseReference): C  = get(checkKey(ref.targetKey).instanceIndex)
    operator fun get(ref: CReference): C  = get(checkKey(ref.targetKey).instanceIndex)
    operator fun get(key: ComponentKey): C  = get(checkKey(key).instanceIndex)
    operator fun get(index: Int): C

    fun exists(name: String): Boolean = getKey(name).instanceIndex >= 0
    fun exists(key: ComponentKey): Boolean = exists(checkKey(key).instanceIndex)
    fun exists(index: Int): Boolean

    fun load(c: C) = load(c.index)
    fun load(name: String) = load(getKey(name).instanceIndex)
    fun load(key: ComponentKey) = load(key.instanceIndex)
    fun load(index: Int)

    fun activate(c: C) = activate(c.index)
    fun activate(name: String) = activate(getKey(name).instanceIndex)
    fun activate(key: ComponentKey) = activate(key.instanceIndex)
    fun activate(index: Int)

    fun deactivate(c: C) = deactivate(c.index)
    fun deactivate(name: String) = deactivate(getKey(name).instanceIndex)
    fun deactivate(key: ComponentKey) = deactivate(key.instanceIndex)
    fun deactivate(index: Int)

    fun dispose(c: C) = dispose(c.index)
    fun dispose(name: String) = dispose(getKey(name).instanceIndex)
    fun dispose(key: ComponentKey) = dispose(key.instanceIndex)
    fun dispose(index: Int)

    fun delete(c: C) = delete(c.index)
    fun delete(name: String) = delete(getKey(name).instanceIndex)
    fun delete(key: ComponentKey) = delete(checkKey(key).instanceIndex)
    fun delete(index: Int)

    val componentEventType: Event.EventType
    fun registerComponentListener(listener: ComponentEventListener) =
        Engine.registerListener(componentEventType, listener)
    fun disposeComponentListener(listener: ComponentEventListener) =
        Engine.disposeListener(componentEventType, listener)

    fun findFirst(filter: (C) -> Boolean): C? {
        var index = getNextIndex(0)
        while (index >= 0) {
            val c = this[index]
            if (filter(c)) return c
            index = getNextIndex(index + 1)
        }
        return null
    }

    fun forEachDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        var index = getNextIndex(0)
        while (index >= 0) {
            val c = this[index]
            if (filter(c))
                process(c)
            index = getNextIndex(index + 1)
        }
    }

    fun forEachActiveDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        var i = getNextActiveIndex(0)
        while (i >= 0) {
            val component = this[i]
            if (filter(component))
                process(component)
            i = getNextActiveIndex(i + 1)
        }
    }

    private fun checkKey(key: ComponentKey): ComponentKey =
        if (this.aspectIndex != key.type.aspectIndex)
            throw IllegalArgumentException("Component type mismatch: ${this.aspectName} | ${key.type.aspectName} ")
        else key
}

abstract class ComponentSystem<C : Component>(
    final override val systemName: String
) : IComponentSystem<C> {

    private var typeAspect: Aspect = Component.COMPONENT_TYPE_ASPECTS.createAspect(systemName)

    final override val typeName: String = systemName
    final override val subTypeName: String = systemName
    final override val aspectIndex: Int = typeAspect.aspectIndex
    final override val aspectName: String = typeAspect.aspectName
    final override val aspectType: AspectType = typeAspect.aspectType

    init { ComponentSystem.registerSystem(this) }

    internal val COMPONENT_KEY_MAPPING: MutableMap<String, ComponentKey> =  HashMap()
    internal val COMPONENT_MAPPING: DynArray<C> = DynArray(50, 100) { size -> allocateArray(size) }
    internal val ACTIVE_COMPONENT_MAPPING: BitSet = BitSet()
    private val EVENT_POOL = ArrayDeque<ComponentEvent>()

    // **** MISC ****
    // **************
    internal abstract fun allocateArray(size: Int): Array<C?>

    override val hasComponents: Boolean
        get() = !COMPONENT_MAPPING.isEmpty
    override val hasActiveComponents: Boolean
        get() = !ACTIVE_COMPONENT_MAPPING.isEmpty

    override fun toString(): String =
        "System: $typeName" +
        "\n  Key Mapping: ${COMPONENT_KEY_MAPPING.values}\n" +
        "  Component Mapping: ${mappingToString()}\n"

    fun mappingToString(): String {
        val builder = StringBuilder()
        COMPONENT_MAPPING.forEach {
            builder.append("\n    $it")
        }
        return builder.toString()
    }
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

    // **** Builder ****
    // *****************
    protected abstract fun create(): C
    override fun build(configure: C.() -> Unit): ComponentKey {
        val comp: C = create()
        comp.also(configure)
        val key = registerComponent(comp)
        initComponent(comp)
        return key
    }

    override fun buildAndGet(configure: C.() -> Unit): C {
        val comp: C = create()
        comp.also(configure)
        registerComponent(comp)
        initComponent(comp)
        return comp
    }

    internal fun initComponent(comp: C) {
        if(comp.initialized) return
        comp.iInitialize()
        comp.initialized = true
        send(comp.key, ComponentEventType.INITIALIZED)
        if (comp.autoLoad)
            load(comp)
        if (comp.autoActivation)
            activate(comp)
    }

    override fun clearSystem() {

        COMPONENT_MAPPING.forEach {
            if (!it.name.contains(STATIC_COMPONENT_MARKER) && it.index >= 0)
                this.delete(it.index)
        }


        try {
            val keys = COMPONENT_KEY_MAPPING
                .filter { it.value.instanceIndex < 0 }
                .map { it.key }
            keys.forEach { COMPONENT_KEY_MAPPING.remove(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ACTIVE_COMPONENT_MAPPING.clear()
        EVENT_POOL.clear()
    }

    // **** Component Handling ****
    // ****************************
    internal open fun registerComponent(c: C): ComponentKey {
        if (checkComponentNameClash(c.name)) {
            c.iDelete()
            throw IllegalArgumentException("Key with same name already exists")
        }

        COMPONENT_MAPPING[c.index] = c

        val key = when {
            // If the component has no name, an ad-hoc ComponentRef is being created but not stored on the name mapping
            (c.name == NO_NAME) -> ComponentKey(NO_NAME, c.componentType)
            // get existing key and update with component index
            (c.name in COMPONENT_KEY_MAPPING) -> COMPONENT_KEY_MAPPING[c.name]!!
            // create new key and store it in the name mapping
            else -> {
                val key = ComponentKey(c.name, c.componentType)
                COMPONENT_KEY_MAPPING[c.name] = key
                key
            }
        }

        key.instanceIndex = c.index
        c.key = key
        return key
    }

    fun registerAsSingleton(component: C, static: Boolean = false) {
        val namePrefix = component::class.simpleName!! + SINGLETON_MARKER
        COMPONENT_MAPPING.forEach {
            if (it.name != NO_NAME && it.name.startsWith(namePrefix))
                throw IllegalStateException("$namePrefix is singleton and already exists")
        }

        component.onStateChange = true
        val name = namePrefix + if (static) STATIC_COMPONENT_MARKER else ""
        component.name = name
        registerComponent(component)
        component.iInitialize()
        component.initialized = true
        component.onStateChange = false
        send(component.key, ComponentEventType.INITIALIZED)
        if (component.autoActivation)
            activate(component.index)
    }

    internal open fun unregisterComponent(index: Int) {
        val removed = COMPONENT_MAPPING.remove(index)
        if (removed != null && removed.name != NO_NAME)
            COMPONENT_KEY_MAPPING[removed.name]?.instanceIndex = -1
    }

    override fun getNextIndex(fromIndex: Int): Int = COMPONENT_MAPPING.nextIndex(fromIndex)
    override fun getNextActiveIndex(fromIndex: Int) = ACTIVE_COMPONENT_MAPPING.nextSetBit(fromIndex)

    override operator fun get(index: Int): C = COMPONENT_MAPPING[index]
        ?: throw IllegalArgumentException("No component for index: $index on system: $typeName")

    override fun exists(index: Int): Boolean = COMPONENT_MAPPING.contains(index)
    override fun load(index: Int) {
        if (!checkIndex(index)) return
        val comp =  this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to load")
        if (comp.onStateChange || comp.loaded) return

        comp.onStateChange = true
        comp.stateChangeProcessing(Apply.BEFORE, ComponentEventType.LOADED)
        comp.iLoad()
        comp.loaded = true
        comp.onStateChange = false
        send(comp.key, ComponentEventType.LOADED)
        comp.stateChangeProcessing(Apply.AFTER, ComponentEventType.LOADED)
    }

    override fun activate(index: Int) {
        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to activate")
        if (comp.onStateChange || comp.active) return

        if (!comp.loaded) // load first before activation
            load(index)

        comp.onStateChange = true
        comp.stateChangeProcessing(Apply.BEFORE, ComponentEventType.ACTIVATED)
        comp.iActivate()
        comp.active = true
        comp.onStateChange = false
        ACTIVE_COMPONENT_MAPPING[index] = true
        send(comp.key, ComponentEventType.ACTIVATED)
        comp.stateChangeProcessing(Apply.AFTER, ComponentEventType.ACTIVATED)
    }

    override fun deactivate(index: Int) {
        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to activate")
        if (comp.onStateChange || !comp.active) return

        comp.onStateChange = true
        comp.stateChangeProcessing(Apply.BEFORE, ComponentEventType.DEACTIVATED)
        comp.iDeactivate()
        comp.active = false
        comp.onStateChange = false
        ACTIVE_COMPONENT_MAPPING[index] = false
        send(comp.key, ComponentEventType.DEACTIVATED)
        comp.stateChangeProcessing(Apply.AFTER, ComponentEventType.DEACTIVATED)
    }

    override fun dispose(index: Int) {
        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to dispose")
        if (comp.onStateChange || !comp.loaded) return

        if (comp.active) // deactivate first if still active
            deactivate(index)

        comp.onStateChange = true
        comp.stateChangeProcessing(Apply.BEFORE, ComponentEventType.DISPOSED)
        comp.iDispose()
        comp.loaded = false
        comp.onStateChange = false
        send(comp.key, ComponentEventType.DISPOSED)
        comp.stateChangeProcessing(Apply.AFTER, ComponentEventType.DISPOSED)
    }

    override fun delete(index: Int) {
        if (!checkIndex(index)) return
        val comp = this[index]
        if (comp.onStateChange) return

        if (comp.loaded) // dispose first when still loaded
            dispose(index)

        comp.onStateChange = true
        comp.stateChangeProcessing(Apply.BEFORE, ComponentEventType.DELETED)
        comp.iDelete()
        comp.initialized = false
        comp.onStateChange = false
        send(comp.key, ComponentEventType.DELETED)
        unregisterComponent(index)
        comp.iDisposeIndex()
        comp.stateChangeProcessing(Apply.AFTER, ComponentEventType.DELETED)
    }

    fun checkIndex(index: Int): Boolean  {
        if (index < 0)
            println("!!! No component instance defined (index < 0) $typeName - $subTypeName. Ignore it !!!")
        return (index >= 0)
    }

    // **** ComponentKey handling ****
    // *******************************
    override fun createKey(name: String): ComponentKey {
        if (name == NO_NAME)
            throw IllegalArgumentException("Key with no name")
        if (name in COMPONENT_KEY_MAPPING)
            throw IllegalArgumentException("Key with same name already exists")
        val newKey = ComponentKey(name, this)
        COMPONENT_KEY_MAPPING[name] = newKey
        return newKey
    }
    override fun hasKey(name: String): Boolean = name in COMPONENT_KEY_MAPPING

    override fun getOrCreateKey(name: String): ComponentKey =
        if (hasKey(name))
            COMPONENT_KEY_MAPPING[name]!!
        else
            createKey(name)

    override fun getKey(name: String): ComponentKey =
        if (hasKey(name))
            COMPONENT_KEY_MAPPING[name]!!
        else
            NO_COMPONENT_KEY

    override fun getKey(index: Int): ComponentKey =
        COMPONENT_MAPPING[index]?.key ?: NO_COMPONENT_KEY

    private fun checkComponentNameClash(name: String): Boolean =
        (COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1) != -1

    // **** Event Handling ****
    // ************************
    override val componentEventType: Event.EventType = Event.EventType("$typeName Event")
    internal fun send(key: ComponentKey, eventType: ComponentEventType) {
        val event = if (!EVENT_POOL.isEmpty())
            EVENT_POOL.removeFirst()
        else
            ComponentEvent(componentEventType)
        event.key = key
        event.componentEventType = eventType
        Engine.notify(event)
        event.key = NO_COMPONENT_KEY
        EVENT_POOL.addFirst(event)
    }

    override fun registerComponentListener(listener: ComponentEventListener) =
        Engine.registerListener(componentEventType, listener)
    override fun disposeComponentListener(listener: ComponentEventListener) =
        Engine.disposeListener(componentEventType, listener)

    companion object {
        const val STATIC_COMPONENT_MARKER = "_STAT_"
        private const val SINGLETON_MARKER = "_SINGL_"

        private val COMPONENT_SYSTEM_MAPPING = mutableMapOf<String, IComponentSystem<*>>()
        internal fun registerSystem(system: IComponentSystem<*>) {
            COMPONENT_SYSTEM_MAPPING[system.subTypeName] = system
        }

        @Suppress("UNCHECKED_CAST")
        fun <C : Component> getComponentBuilder(typeName: String): ComponentBuilder<C> =
            COMPONENT_SYSTEM_MAPPING[typeName] as ComponentBuilder<C>

        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(type: ComponentType<C>): IComponentSystem<C> =
            COMPONENT_SYSTEM_MAPPING[type.subTypeName] as IComponentSystem<C>


        fun exists(key: ComponentKey): Boolean = this[key.type].exists(key)
        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(key: ComponentKey): C = this[key.type][key] as C
        operator fun <C : Component> get(type: ComponentType<C>, name: String): C = this[type][name]
        operator fun <C : Component> get(type: ComponentType<C>, index: Int): C = this[type][index]

        fun load(key: ComponentKey) = this[key.type].load(key)
        fun activate(key: ComponentKey) = this[key.type].activate(key)
        fun deactivate(key: ComponentKey) = this[key.type].deactivate(key)
        fun dispose(key: ComponentKey) = this[key.type].dispose(key)
        fun delete(key: ComponentKey) = this[key.type].delete(key)

        fun clearSystems() = COMPONENT_SYSTEM_MAPPING.forEach { it.value.clearSystem() }
        
        fun dumpInfo() {
            println("---- System Info ---------------------------------------------------------------------------------")
            COMPONENT_SYSTEM_MAPPING.values.forEach { println(it) }
            println("--------------------------------------------------------------------------------------------------")
        }
    }
}

class ComponentEvent internal constructor(override val eventType: EventType): Event<ComponentEventListener>() {
    internal var key: ComponentKey = NO_COMPONENT_KEY
    internal lateinit var componentEventType: ComponentEventType
    override fun notify(listener: ComponentEventListener) = listener(key, componentEventType)
}

abstract class ComponentSubTypeBuilder<C : Component, CC : C>(
     val system: ComponentSystem<C>,
     final override val subTypeName: String
 ) : ComponentBuilder<CC>, IComponentSystem<CC> {

    private val subTypeFilter: (C) -> Boolean = { it.componentType.subTypeName == subTypeName }

    final override val systemName: String = subTypeName
    final override val typeName: String = system.typeName
    final override val aspectIndex: Int = system.aspectIndex
    final override val aspectName: String = system.aspectName
    final override val aspectType: AspectType = system.aspectType

    init { ComponentSystem.registerSystem(this) }

    override val componentEventType: Event.EventType = system.componentEventType
    override val hasComponents: Boolean
        get() = getNextActiveIndex(0) >= 0
    override val hasActiveComponents: Boolean
        get() = getNextActiveIndex(0) >= 0

    override fun clearSystem() = system.forEachDo(subTypeFilter) { system.delete(it) }
    override fun getNextIndex(fromIndex: Int): Int {
        var index = system.getNextIndex(fromIndex)
        while (index >= 0 && !subTypeFilter(system[index]))
            index = system.getNextIndex(index + 1)
        return index
    }

    override fun getNextActiveIndex(fromIndex: Int): Int {
        var index = system.getNextActiveIndex(fromIndex)
        while (index >= 0 && !subTypeFilter(system[index]))
            index = system.getNextActiveIndex(index + 1)
        return index
    }

    override fun createKey(name: String) = system.createKey(name)
    override fun hasKey(name: String) = system.hasKey(name)
    override fun getOrCreateKey(name: String) = system.getOrCreateKey(name)
    override fun getKey(name: String) = system.getKey(name)
    override fun getKey(index: Int) = system.getKey(index)
    @Suppress("UNCHECKED_CAST")
    override fun get(index: Int): CC = system[index] as CC
    override fun exists(index: Int) = system.exists(index)
    override fun load(index: Int) = system.load(index)
    override fun activate(index: Int) = system.activate(index)
    override fun deactivate(index: Int) = system.deactivate(index)
    override fun dispose(index: Int) = system.dispose(index)
    override fun delete(index: Int) = system.delete(index)
    override fun build(configure: CC.() -> Unit): ComponentKey {
        val comp: CC = create()
        comp.also(configure)
        val key = system.registerComponent(comp)
        system.initComponent(comp)
        return key
    }
    override fun buildAndGet(configure: CC.() -> Unit): CC {
        val comp: CC = create()
        comp.also(configure)
        system.registerComponent(comp)
        system.initComponent(comp)
        return comp
    }

    override fun toString(): String {
        val result = StringBuilder("SubTypeSystem: $subTypeName\n  Component Mapping:\n")
        forEachDo { result.append( "    $it\n") }
        return result.toString()
    }

    abstract fun create(): CC
}

