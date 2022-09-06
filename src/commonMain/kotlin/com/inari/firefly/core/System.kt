package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.game.composite.Composite
import com.inari.util.NO_NAME
import com.inari.util.TRUE_PREDICATE
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.collection.BitSet
import com.inari.util.collection.BitSetIterator
import com.inari.util.collection.DynArray
import com.inari.util.event.Event

interface System {
    fun clearSystem()
}

abstract class ComponentSystem<C : Component>(
    name: String,
) : ComponentBuilder<C>, ComponentType<C>, System {

    private lateinit var typeAspect: Aspect

    init {
        typeAspect = Component.COMPONENT_TYPE_ASPECTS.createAspect(name)
        COMPONENT_SYSTEM_MAPPING[typeAspect.aspectIndex] = this
    }

    final override val typeName: String = name
    final override val subTypeName: String = name
    final override val aspectIndex: Int = typeAspect.aspectIndex
    final override val aspectName: String = typeAspect.aspectName
    final override val aspectType: AspectType = typeAspect.aspectType
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

    internal val COMPONENT_KEY_MAPPING: MutableMap<String, ComponentKey> =  HashMap()
    internal val COMPONENT_MAPPING: DynArray<C> = DynArray(50, 100) { size -> allocateArray(size) }
    internal val ACTIVE_COMPONENT_MAPPING: BitSet = BitSet()
    private val EVENT_POOL = ArrayDeque<ComponentEvent>()

    protected abstract fun create(): C
    override fun build(configure: C.() -> Unit): ComponentKey {
        val comp: C = create()
        comp.also(configure)
        val key = registerComponent(comp)
        comp.iInitialize()
        send(comp.index, ComponentEventType.INITIALIZED)
        return key
    }

    override fun buildAndGet(configure: C.() -> Unit): C {
        val comp: C = create()
        comp.also(configure)
        registerComponent(comp)
        comp.iInitialize()
        send(comp.index, ComponentEventType.INITIALIZED)
        return comp
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

    // **** MISC ****
    // **************
    internal abstract fun allocateArray(size: Int): Array<C?>

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

    // **** Component Handling ****
    // ****************************
    internal open fun registerComponent(c: C): ComponentKey {
        if (checkComponentNameClash(c.name)) {
            c.iDelete()
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

    fun registerAsSingleton(component: C, static: Boolean = false) {
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
    operator fun get(ref: CLooseReference): C  = get(checkKey(ref.targetKey).instanceIndex)
    operator fun get(ref: CReference): C  = get(checkKey(ref.targetKey).instanceIndex)
    operator fun get(key: ComponentKey): C  = get(checkKey(key).instanceIndex)
    operator fun get(index: Int): C = COMPONENT_MAPPING[index]
        ?: throw IllegalArgumentException("No component for index: $index on system: $typeName")

    operator fun <CC : C> get(name: String, subType: ComponentSubTypeSystem<C, CC>): CC =
        get(COMPONENT_KEY_MAPPING[name]!!, subType)
    operator fun <CC : C> get(key: ComponentKey, subType: ComponentSubTypeSystem<C, CC>): CC  =
        get(checkKey(key).instanceIndex, subType)
    @Suppress("UNCHECKED_CAST")
    operator fun  <CC : C> get(index: Int, subType: ComponentSubTypeSystem<C, CC>): CC =
        COMPONENT_MAPPING[index]!! as CC

    fun exists(name: String): Boolean = name in COMPONENT_KEY_MAPPING && exists(COMPONENT_KEY_MAPPING[name]!!)
    fun exists(key: ComponentKey): Boolean = exists(checkKey(key).instanceIndex)
    fun exists(index: Int): Boolean = COMPONENT_MAPPING.contains(index)

    fun load(name: String) = load(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1)
    fun load(key: ComponentKey) = load(key.instanceIndex)
    fun load(index: Int) {
        checkIndex(index)
        val comp =  this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to load")
        if (comp.loaded)
            return

        comp.iLoad()
        send(index, ComponentEventType.LOADED)
    }

    fun activate(name: String) = activate(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1)
    fun activate(key: ComponentKey) = activate(key.instanceIndex)
    fun activate(index: Int) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to activate")
        if (comp.active)
            return

        comp.iActivate()
        ACTIVE_COMPONENT_MAPPING[index] = true
        send(index, ComponentEventType.ACTIVATED)
    }

    fun deactivate(name: String) = deactivate(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1)
    fun deactivate(key: ComponentKey) = deactivate(key.instanceIndex)
    fun deactivate(index: Int) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to activate")
        if (!comp.active)
            return

        comp.iDeactivate()
        ACTIVE_COMPONENT_MAPPING[index] = false
        send(index, ComponentEventType.DEACTIVATED)
    }

    fun dispose(name: String) = dispose(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1)
    fun dispose(key: ComponentKey) = dispose(key.instanceIndex)
    fun dispose(index: Int) {
        checkIndex(index)
        val comp = this[index]
        if (!comp.initialized)
            throw IllegalStateException("Component in illegal state to dispose")
        if (!comp.loaded)
            return

        comp.iDispose()
        send(index, ComponentEventType.DISPOSED)
    }

    fun delete(c: C) = delete(c.index)
    fun delete(name: String) = delete(COMPONENT_KEY_MAPPING[name]?.instanceIndex ?: -1)
    fun delete(key: ComponentKey) = delete(checkKey(key).instanceIndex)
    fun delete(index: Int) {
        checkIndex(index)
        val comp = this[index]
        comp.iDelete()
        send(index, ComponentEventType.DELETED)
        unregisterComponent(index)
    }

    fun findFirst(filter: (C) -> Boolean): C? {
        COMPONENT_MAPPING.forEach {
            if (filter(it))
                return it
        }
        return null
    }

    fun forEachDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        COMPONENT_MAPPING.forEach {
            if (filter(it))
                process(it)
        }
    }

    fun forEachActiveDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        var i = Composite.ACTIVE_COMPONENT_MAPPING.nextSetBit(0)
        while (i >= 0) {
            val component = COMPONENT_MAPPING[i]!!
            if (filter(component))
                process(component)
            i = Composite.ACTIVE_COMPONENT_MAPPING.nextSetBit(i + 1)
        }
    }

    private fun checkKey(key: ComponentKey): ComponentKey =
        if (this.aspectIndex != key.type.aspectIndex) throw IllegalArgumentException("Component type mismatch!")
        else key

    fun checkIndex(index: Int) {
        if (index < 0)
            throw IllegalArgumentException("No component instance defined (index < 0) $typeName - $subTypeName")
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

    fun getKey(name: String, createIfNotExists: Boolean = false): ComponentKey =
        if (hasKey(name))
            COMPONENT_KEY_MAPPING[name]!!
        else if (createIfNotExists)
            createKey(name)
        else
            throw NoSuchElementException("No ComponentKey found for: $name type: ${this.typeName}")

    fun getKey(index: Int): ComponentKey =
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
    fun send(compIndex: CompIndex, eventType: ComponentEventType) {
        val event = if (!EVENT_POOL.isEmpty())
            EVENT_POOL.removeFirst()
        else
            ComponentEvent(componentEventType)
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
        internal val COMPONENT_BUILDER_MAPPING: DynArray<ComponentBuilder<*>> = DynArray.of()

        internal fun clearSystems(type: ComponentType<*>) = COMPONENT_SYSTEM_MAPPING[type.aspectIndex]?.clearSystem()
        internal fun clearAllSystems() {
            COMPONENT_SYSTEM_MAPPING.forEach { it.clearSystem() }
        }

        @Suppress("UNCHECKED_CAST")
        fun <C : Component> getComponentBuilder(typeName: String): ComponentBuilder<C> =
            COMPONENT_SYSTEM_MAPPING.find { system -> system.typeName == typeName } as ComponentBuilder<C>

        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(type: ComponentType<C>): ComponentSystem<C> =
            COMPONENT_SYSTEM_MAPPING[type.aspectIndex] as ComponentSystem<C>


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

class ComponentEvent internal constructor(override val eventType: EventType): Event<ComponentEventListener>() {
    internal var index: CompIndex = -1
    internal lateinit var componentEventType: ComponentEventType
    override fun notify(listener: ComponentEventListener) = listener(index, componentEventType)
}

abstract class ComponentSubTypeSystem<C : Component, CC : C>(
    val system: ComponentSystem<C>,
    override val subTypeName: String
) : ComponentBuilder<CC>, ComponentType<CC> {

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

    final override val aspectName = system.aspectName
    final override val aspectType = system.aspectType
    final override val aspectIndex = system.aspectIndex
    final override val typeName = system.typeName

    protected abstract fun create(): CC

    override fun build(configure: CC.() -> Unit): ComponentKey {
        @Suppress("UNCHECKED_CAST")
        val comp: CC = create()
        comp.also(configure)
        val key = system.registerComponent(comp)
        comp.iInitialize()
        system.send(comp.index, ComponentEventType.INITIALIZED)
        return key
    }

    override fun buildAndGet(configure: CC.() -> Unit): CC {
        @Suppress("UNCHECKED_CAST")
        val comp: CC = create()
        comp.also(configure)
        system.registerComponent(comp)
        comp.iInitialize()
        system.send(comp.index, ComponentEventType.INITIALIZED)
        return comp
    }

    fun checkIndex(index: Int) = system.checkIndex(index)

    fun getKey(name: String, createIfNotExists: Boolean) = system.getKey(name, createIfNotExists)
    fun getKey(index: Int) = system.getKey(index)

    @Suppress("UNCHECKED_CAST")
    operator fun get(name: String): CC = system[name] as CC
    @Suppress("UNCHECKED_CAST")
    operator fun get(key: ComponentKey): CC  = system[key] as CC
    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): CC = system[index] as CC

    fun exists(name: String): Boolean = system.exists(name)
    fun exists(key: ComponentKey): Boolean = system.exists(key)
    fun exists(index: Int): Boolean = system.exists(index)

    fun load(name: String) = system.load(name)
    fun load(key: ComponentKey) = system.load(key)
    fun load(index: Int) =  system.load(index)

    fun activate(name: String) = system.activate(name)
    fun activate(key: ComponentKey) = system.activate(key)
    fun activate(index: Int) = system.activate(index)

    fun deactivate(name: String) = system.deactivate(name)
    fun deactivate(key: ComponentKey) = system.deactivate(key)
    fun deactivate(index: Int) = system.deactivate(index)

    fun dispose(name: String) = system.dispose(name)
    fun dispose(key: ComponentKey) = system.dispose(key)
    fun dispose(index: Int) = system.dispose(index)

    fun delete(name: String) = system.delete(name)
    fun delete(key: ComponentKey) = system.delete(key)
    fun delete(index: Int) = system.delete(index)

    @Suppress("UNCHECKED_CAST")
    fun forEachDo(
        filter: (CC) -> Boolean = TRUE_PREDICATE,
        process: (CC) -> Unit
    ) {
        this.system.forEachDo({ c -> c.componentType.subTypeName == subTypeName && filter(c as CC)}) {
            process(it as CC)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun forEachActiveDo(
        filter: (CC) -> Boolean = TRUE_PREDICATE,
        process: (CC) -> Unit
    ) {
        this.system.forEachActiveDo({ c -> c.componentType.subTypeName == subTypeName && filter(c as CC)}) {
            process(it as CC)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun findFirst(filter: (CC) -> Boolean = TRUE_PREDICATE): CC? {
        val comp = this.system.findFirst {
            it.componentType.subTypeName == subTypeName && filter(it as CC)
        }
        return if (comp == null) null else comp as CC
    }

    @Suppress("UNCHECKED_CAST")
    fun findFirstActive(filter: (CC) -> Boolean = TRUE_PREDICATE): CC? {
        val comp = this.system.findFirst {
            it.componentType.subTypeName == subTypeName && it.active && filter(it as CC)
        }
        return if (comp == null) null else comp as CC
    }
}


