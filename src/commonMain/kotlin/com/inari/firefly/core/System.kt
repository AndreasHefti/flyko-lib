package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.core.Component.*
import com.inari.firefly.core.ApplyPolicies.*
import com.inari.firefly.core.ComponentSystem.Companion.COMPONENT_BUILDER_MAPPING
import com.inari.util.NO_NAME
import com.inari.util.aspect.Aspect
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetIterator
import com.inari.util.collection.DynArray
import com.inari.util.event.Event

interface System {
    fun clearSystem()
}

abstract class ComponentSystem<C : Component>(
    typeName: String,
) : ComponentType<C>(typeName), SubComponentSystemAdapter<C>, System {

    protected val COMPONENT_KEY_MAPPING: MutableMap<String, ComponentKey> =  HashMap()
    protected val COMPONENT_MAPPING: DynArray<C> = DynArray(50, 100, this::allocateArray)
    protected val ACTIVE_COMPONENT_MAPPING: BitSet = BitSet()
    private val EVENT_POOL = ArrayDeque<InternalComponentEvent>()

    init {
        COMPONENT_SYSTEM_MAPPING[super.aspectIndex] = this
    }

    override fun clearSystem() {
        COMPONENT_MAPPING.forEach {
            if (!it.name.contains(STATIC_COMPONENT_MARKER))
                this.delete(it.index)
        }

        val keys = COMPONENT_KEY_MAPPING
            .filter { it.value.instanceIndex < 0 }
            .map { it.key }
        keys.forEach { COMPONENT_KEY_MAPPING.remove(it) }

        ACTIVE_COMPONENT_MAPPING.clear()
        EVENT_POOL.clear()
    }

    // **** MISC ****
    // **************
    internal abstract fun allocateArray(size: Int): Array<C?>
    internal abstract fun create(): C

    val hasComponents: Boolean
        get() = !COMPONENT_MAPPING.isEmpty
    val hasActiveComponents: Boolean
        get() = !ACTIVE_COMPONENT_MAPPING.isEmpty

    override fun toString(): String =
        "Name: $typeName\n" +
        "Key Mapping: ${COMPONENT_KEY_MAPPING.values}\n" +
        "Component Mapping: ${mappingToString()}\n"

    fun mappingToString(): String {
        val builder = StringBuilder()
        COMPONENT_MAPPING.forEach {
            builder.append("\n    $it")
        }
        return builder.toString()
    }

    // **** Component Builder ****
    // ***************************
    override val builder: ComponentBuilder<C> = object : ComponentBuilder<C>(this) {
        override fun create() = this@ComponentSystem.create()
    }
    fun build(configure: C.() -> Unit): ComponentKey = builder.build(configure)
    fun buildAndGet(configure: C.() -> Unit): C = builder.buildAndGet(configure)
    fun buildActive(configure: C.() -> Unit): ComponentKey = builder.buildActive(configure)
    fun buildAndGetActive(configure: C.() -> Unit): C = builder.buildAndGetActive(configure)

    // **** Component Handling ****
    // ****************************
    internal open fun registerComponent(c: C): ComponentKey {
        if (checkComponentNameClash(c.name)) {
            c.iDelete(DEFAULT_DELETE)
            throw IllegalArgumentException("Key with same name already exists")
        }

        COMPONENT_MAPPING[c.index] = c

        return when {
            // If the component has no name, an ad-hoc ComponentRef is being created but not stored on the name mapping
            (c.name == NO_NAME) -> {
                val key = ComponentKey(NO_NAME, c.componentType)
                key.instanceIndex = c.index
                key
            }
            // get existing key and update with component index
            (c.name in COMPONENT_KEY_MAPPING) -> {
                val key = COMPONENT_KEY_MAPPING[c.name]!!
                key.instanceIndex = c.index
                key
            }
            // create new key and store it in the name mapping
            else -> {
                val key = ComponentKey(c.name, c.componentType)
                key.instanceIndex = c.index
                COMPONENT_KEY_MAPPING[c.name] = key
                key
            }
        }
    }

    fun registerAsSingleton(component: C, static: Boolean ) {
        val namePrefix = component::class.simpleName!! + SINGLETON_MARKER
        COMPONENT_MAPPING.forEach {
            if (it.name != NO_NAME && it.name.startsWith(namePrefix))
                throw IllegalStateException("$namePrefix is singleton and already exists")
        }

        val name = namePrefix + if (static) STATIC_COMPONENT_MARKER else ""
        component.name = name
        registerComponent(component)
        component.iInitialize()
        send(component.index, ComponentEventType.INITIALIZED)
    }

    internal open fun unregisterComponent(index: Int) {
        val removed = COMPONENT_MAPPING.remove(index)
        if (removed != null && removed.name != NO_NAME)
            COMPONENT_KEY_MAPPING[removed.name]?.instanceIndex = -1
    }

    fun getNextActiveIndex(fromIndex: Int) = ACTIVE_COMPONENT_MAPPING.nextSetBit(fromIndex)
    fun getActiveIndexIterator(): com.inari.util.IntIterator = BitSetIterator(ACTIVE_COMPONENT_MAPPING)

    operator fun get(name: String): C = get(COMPONENT_KEY_MAPPING[name]
        ?: throw IllegalArgumentException("No component for name: $name found on system: $typeName"))
    operator fun get(key: ComponentKey): C  = get(checkKey(key).instanceIndex)
    override operator fun get(index: Int): C = COMPONENT_MAPPING[index]
        ?: throw IllegalArgumentException("No component for index: $index on system: $typeName")

    operator fun <CC : C> get(name: String, subType: SubComponentSystemAdapter<CC>): CC =
        get(COMPONENT_KEY_MAPPING[name]!!, subType)
    operator fun <CC : C> get(key: ComponentKey, subType: SubComponentSystemAdapter<CC>): CC  =
        get(checkKey(key).instanceIndex, subType)
    @Suppress("UNCHECKED_CAST")
    operator fun  <CC : C> get(index: Int, subType: SubComponentSystemAdapter<CC>): CC =
        COMPONENT_MAPPING[index]!! as CC

    fun exists(name: String): Boolean = name in COMPONENT_KEY_MAPPING && exists(COMPONENT_KEY_MAPPING[name]!!)
    fun exists(key: ComponentKey): Boolean = exists(checkKey(key).instanceIndex)
    fun exists(index: Int): Boolean = COMPONENT_MAPPING.contains(index)

    fun load(name: String, policy: ApplyPolicy = DEFAULT_LOAD) = load(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1, policy)
    fun load(key: ComponentKey, policy: ApplyPolicy = DEFAULT_LOAD) = load(key.instanceIndex, policy)
    fun load(index: Int, policy: ApplyPolicy = DEFAULT_LOAD) {
        checkIndex(index)
        val comp =  this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to load")
        if (comp.loaded)
            return

        comp.iLoad(policy)
        send(index, ComponentEventType.LOADED)
    }

    fun activate(name: String, policy: ApplyPolicy = DEFAULT_ACTIVATE) = activate(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1, policy)
    fun activate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_ACTIVATE) = activate(key.instanceIndex, policy)
    fun activate(index: Int, policy: ApplyPolicy = DEFAULT_ACTIVATE) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to activate")
        if (comp.active)
            return

        comp.iActivate(policy)
        ACTIVE_COMPONENT_MAPPING[index] = true
        send(index, ComponentEventType.ACTIVATED)
    }

    fun deactivate(name: String, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = deactivate(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1, policy)
    fun deactivate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = deactivate(key.instanceIndex, policy)
    fun deactivate(index: Int, policy: ApplyPolicy = DEFAULT_DEACTIVATE) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to activate")
        if (!comp.active)
            return

        comp.iDeactivate(policy)
        ACTIVE_COMPONENT_MAPPING[index] = false
        send(index, ComponentEventType.DEACTIVATED)
    }

    fun dispose(name: String, policy: ApplyPolicy = DEFAULT_DISPOSE) = dispose(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1, policy)
    fun dispose(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DISPOSE) = dispose(key.instanceIndex, policy)
    fun dispose(index: Int, policy: ApplyPolicy = DEFAULT_DISPOSE) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to dispose")
        if (!comp.loaded)
            return

        comp.iDispose(policy)
        send(index, ComponentEventType.DISPOSED)
    }

    fun delete(c: C, policy: ApplyPolicy = DEFAULT_DELETE) = delete(c.index, policy)
    fun delete(name: String, policy: ApplyPolicy = DEFAULT_DELETE) = delete(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1, policy)
    fun delete(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DELETE) = delete(checkKey(key).instanceIndex, policy)
    fun delete(index: Int, policy: ApplyPolicy = DEFAULT_DELETE) {
        checkIndex(index)
        val comp = this[index]
        comp.iDelete(policy)
        send(index, ComponentEventType.DELETED)
        unregisterComponent(index)
    }

    fun doForEach(
        filter: (C) -> Boolean,
        process: (C) -> Unit
    ) {
        COMPONENT_MAPPING.forEach {
            if (filter(it))
                process(it)
        }
    }

    private fun checkKey(key: ComponentKey): ComponentKey =
        if (this.aspectIndex != key.type.aspectIndex) throw IllegalArgumentException("Component type mismatch!")
        else key

    private fun checkIndex(index: Int) {
        if (index < 0)
            throw IllegalArgumentException("No component instance defined (index < 0)")
    }

    // **** ComponentKey handling ****
    // *******************************
    fun createKey(name: String): ComponentKey {
        if (name in COMPONENT_KEY_MAPPING)
            throw IllegalArgumentException("Key with same name already exists")
        val newKey = ComponentKey(name, this)
        COMPONENT_KEY_MAPPING[name] = newKey
        return newKey
    }
    fun hasKey(name: String): Boolean = name in COMPONENT_KEY_MAPPING
    override fun getKey(name: String): ComponentKey = COMPONENT_KEY_MAPPING[name]
        ?: throw NoSuchElementException("No ComponentKey found for: $name type: ${this.typeName}")
    override fun getKey(index: Int): ComponentKey =
        COMPONENT_MAPPING[index]?.let {
            if (it.name != NO_NAME)
                COMPONENT_KEY_MAPPING[it.name]
                    ?: createKey(it.name)
            else {
                throw NoSuchElementException("No ComponentKey found none named components. Name the component first")
//                val ref = ComponentKey(NO_NAME, this)
//                ref.instanceId = index
//                ref
            }
        } ?: NO_COMPONENT_KEY


    private fun checkComponentNameClash(name: String): Boolean =
        (COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1) != -1

    // **** Event Handling ****
    // ************************
    val componentEventType: Event.EventType = Event.EventType("$typeName Event")
    internal class InternalComponentEvent(override val eventType: EventType) : ComponentEvent()
    fun send(compIndex: CompIndex, eventType: ComponentEventType) {
        val event = if (!EVENT_POOL.isEmpty())
            EVENT_POOL.removeFirst()
        else
            InternalComponentEvent(componentEventType)
        event.index = compIndex
        event.componentEventType = eventType
        Engine.notify(event)
        event.index = -1
        EVENT_POOL.addFirst(event)
    }

    fun registerComponentListener(listener: ComponentEventListener) =
        Engine.registerListener(componentEventType, listener)
    fun disposeComponentListener(listener: ComponentEventListener) =
        Engine.disposeListener(componentEventType, listener)

    companion object {
        const val STATIC_COMPONENT_MARKER = "_STAT_"
        private const val SINGLETON_MARKER = "_SINGL_"

        internal val COMPONENT_SYSTEM_MAPPING: DynArray<ComponentSystem<*>> = DynArray.of()
        internal val COMPONENT_BUILDER_MAPPING = mutableMapOf<String, ComponentBuilder<*>>()

        internal fun clearSystems(type: ComponentType<*>) = COMPONENT_SYSTEM_MAPPING[type.aspectIndex]?.clearSystem()
        internal fun clearAllSystems() {
            COMPONENT_SYSTEM_MAPPING.forEach { it.clearSystem() }
        }

        @Suppress("UNCHECKED_CAST")
        fun <C : Component>  getComponentBuilder(componentName: String): ComponentBuilder<C> =
            COMPONENT_BUILDER_MAPPING[componentName] as ComponentBuilder<C>

        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(type: ComponentType<C>): ComponentSystem<C> {
            return COMPONENT_SYSTEM_MAPPING[type.aspectIndex] as ComponentSystem<C>
        }

        fun exists(key: ComponentKey): Boolean = this[key.type].exists(key)
        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(key: ComponentKey): C = this[key.type][key] as C
        operator fun <C : Component> get(type: ComponentType<C>, name: String): C = this[type][name]
        operator fun <C : Component> get(type: ComponentType<C>, index: Int): C = this[type][index]

        fun load(key: ComponentKey, policy: ApplyPolicy = DEFAULT_LOAD) = this[key.type].load(key, policy)
        fun activate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_ACTIVATE) = this[key.type].activate(key, policy)
        fun deactivate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = this[key.type].deactivate(key, policy)
        fun dispose(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DISPOSE) = this[key.type].dispose(key, policy)
        fun delete(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DELETE) = this[key.type].delete(key, policy)

        fun clearSystems() = COMPONENT_SYSTEM_MAPPING.forEach { it.clearSystem() }


        fun dumpInfo() {
            println("---- System Info ---------------------------------------------------------------------------------")
            COMPONENT_SYSTEM_MAPPING.forEach {
                println("System: $it")
            }
            println("--------------------------------------------------------------------------------------------------")
        }
    }
}

abstract class ComponentSubTypeSystem<C : Component, CC : C>(
    system: ComponentSystem<C>,
    subTypeName: String
) : ComponentBuilder<CC>(system), SubComponentSystemAdapter<CC>, Aspect  {

    val subType: ComponentType<CC> = object : ComponentType<CC>(subTypeName) {}
    override val aspectName = subType.aspectName
    override val aspectType = subType.aspectType
    override val aspectIndex = subType.aspectIndex

    override val builder get() = this

    override fun getKey(name: String) = system.getKey(name)
    override fun getKey(index: Int) = system.getKey(index)

    @Suppress("UNCHECKED_CAST")
    operator fun get(name: String): CC = system[name] as CC
    @Suppress("UNCHECKED_CAST")
    operator fun get(key: ComponentKey): CC  = system[key] as CC
    @Suppress("UNCHECKED_CAST")
    override operator fun get(index: Int): CC = system[index] as CC

    fun exists(name: String): Boolean = system.exists(name)
    fun exists(key: ComponentKey): Boolean = system.exists(key)
    fun exists(index: Int): Boolean = system.exists(index)

    fun load(name: String, policy: ApplyPolicy = DEFAULT_LOAD) = system.load(name, policy)
    fun load(key: ComponentKey, policy: ApplyPolicy = DEFAULT_LOAD) = system.load(key, policy)
    fun load(index: Int, policy: ApplyPolicy = DEFAULT_LOAD) =  system.load(index, policy)

    fun activate(name: String, policy: ApplyPolicy = DEFAULT_ACTIVATE) = system.activate(name, policy)
    fun activate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_ACTIVATE) = system.activate(key, policy)
    fun activate(index: Int, policy: ApplyPolicy = DEFAULT_ACTIVATE) = system.activate(index, policy)

    fun deactivate(name: String, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = system.deactivate(name, policy)
    fun deactivate(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = system.deactivate(key, policy)
    fun deactivate(index: Int, policy: ApplyPolicy = DEFAULT_DEACTIVATE) = system.deactivate(index, policy)

    fun dispose(name: String, policy: ApplyPolicy = DEFAULT_DISPOSE) = system.dispose(name, policy)
    fun dispose(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DISPOSE) = system.dispose(key, policy)
    fun dispose(index: Int, policy: ApplyPolicy = DEFAULT_DISPOSE) = system.dispose(index, policy)

    fun delete(name: String, policy: ApplyPolicy = DEFAULT_DELETE) = system.delete(name, policy)
    fun delete(key: ComponentKey, policy: ApplyPolicy = DEFAULT_DELETE) = system.delete(key, policy)
    fun delete(index: Int, policy: ApplyPolicy = DEFAULT_DELETE) = system.delete(index, policy)

}


