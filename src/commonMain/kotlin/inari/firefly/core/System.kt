package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.NO_NAME
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.event.Event

interface System {
    fun clearSystem()
}

abstract class ComponentSystem<C : Component>(
    typeName: String,
) : ComponentType<C>(typeName), SubComponentSystemAdapter<C>, System {

    private val COMPONENT_KEY_MAPPING: MutableMap<String, ComponentKey> =  HashMap()
    private val COMPONENT_MAPPING: DynArray<C> = DynArray(50, 100, this::allocateArray)
    private val ACTIVE_COMPONENT_MAPPING: BitSet = BitSet()
    private val EVENT_POOL = ArrayDeque<InternalComponentEvent>()

    init {
        COMPONENT_SYSTEM_MAPPING[super.aspectIndex] = this
    }

    override fun clearSystem() {
        COMPONENT_MAPPING.forEach { delete(it) }
        COMPONENT_KEY_MAPPING.clear()
        ACTIVE_COMPONENT_MAPPING.clear()
        EVENT_POOL.clear()
    }

    // **** MISC ****
    // **************
    internal abstract fun allocateArray(size: Int): Array<C?>
    internal abstract fun create(): C

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
    internal fun registerComponent(c: C): ComponentKey {
        if (checkComponentNameClash(c.name)) {
            c.iDelete()
            throw IllegalArgumentException("Key with same name already exists")
        }

        COMPONENT_MAPPING[c.index] = c

        return when {
            // If the component has no name, an ad-hoc ComponentRef is being created but not stored on the name mapping
            (c.name == NO_NAME) -> {
                val key = ComponentKey(NO_NAME, c.componentType)
                key.instanceId = c.index
                key
            }
            // get existing key and update with component index
            (c.name in COMPONENT_KEY_MAPPING) -> {
                val key = COMPONENT_KEY_MAPPING[c.name]!!
                key.instanceId = c.index
                key
            }
            // create new key and store it in the name mapping
            else -> {
                val key = ComponentKey(c.name, c.componentType)
                key.instanceId = c.index
                COMPONENT_KEY_MAPPING[c.name] = key
                key
            }
        }
    }

    fun registerAsSingleton(component: C) {
        val name = component::class.simpleName!!
        if (exists(name))
            throw IllegalStateException("$name is singleton and already exists")

        component.name = name
        registerComponent(component)
        component.iInitialize()
        send(component.index, ComponentEventType.INITIALIZED)
    }

    internal fun unregisterComponent(index: Int) {
        val removed = COMPONENT_MAPPING.remove(index)
        if (removed != null && removed.name != NO_NAME)
            COMPONENT_KEY_MAPPING[removed.name]?.instanceId = -1
    }

    operator fun get(name: String): C = get(COMPONENT_KEY_MAPPING[name]!!)
    operator fun get(key: ComponentKey): C  = get(checkKey(key).instanceId)
    override operator fun get(index: Int): C = COMPONENT_MAPPING[index]
        ?: throw IllegalArgumentException("No component for index: $index on system: $typeName")

    operator fun <CC : C> get(name: String, subType: SubComponentSystemAdapter<CC>): CC =
        get(COMPONENT_KEY_MAPPING[name]!!, subType)
    operator fun <CC : C> get(key: ComponentKey, subType: SubComponentSystemAdapter<CC>): CC  =
        get(checkKey(key).instanceId, subType)
    @Suppress("UNCHECKED_CAST")
    operator fun  <CC : C> get(index: Int, subType: SubComponentSystemAdapter<CC>): CC =
        COMPONENT_MAPPING[index]!! as CC

    fun exists(name: String): Boolean = name in COMPONENT_KEY_MAPPING && exists(COMPONENT_KEY_MAPPING[name]!!)
    fun exists(key: ComponentKey): Boolean = exists(checkKey(key).instanceId)
    fun exists(index: Int): Boolean = COMPONENT_MAPPING.contains(index)

    fun load(name: String) = load(COMPONENT_KEY_MAPPING[name]?.instanceId ?: -1)
    fun load(key: ComponentKey) = load(key.instanceId)
    fun load(index: Int) {
        checkIndex(index)
        this[index].iLoad()
        send(index, ComponentEventType.LOADED)
    }

    fun activate(name: String) = activate(COMPONENT_KEY_MAPPING[name]?.instanceId ?: -1)
    fun activate(key: ComponentKey) = activate(key.instanceId)
    fun activate(index: Int) {
        checkIndex(index)
        this[index].iActivate()
        ACTIVE_COMPONENT_MAPPING[index] = true
        send(index, ComponentEventType.ACTIVATED)
    }

    fun deactivate(name: String) = deactivate(COMPONENT_KEY_MAPPING[name]?.instanceId ?: -1)
    fun deactivate(key: ComponentKey) = deactivate(key.instanceId)
    fun deactivate(index: Int) {
        checkIndex(index)
        this[index].iDeactivate()
        ACTIVE_COMPONENT_MAPPING[index] = false
        send(index, ComponentEventType.DEACTIVATED)
    }

    fun dispose(name: String) = dispose(COMPONENT_KEY_MAPPING[name]?.instanceId ?: -1)
    fun dispose(key: ComponentKey) = dispose(key.instanceId)
    fun dispose(index: Int) {
        checkIndex(index)
        this[index].iDispose()
        send(index, ComponentEventType.DISPOSED)
    }

    fun delete(c: C) = delete(c.index)
    fun delete(name: String) = delete(COMPONENT_KEY_MAPPING[name]!!)
    fun delete(key: ComponentKey) = delete(checkKey(key).instanceId)
    fun delete(index: Int) {
        checkIndex(index)
        val comp = this[index]
        send(index, ComponentEventType.DELETED)
        comp.iDelete()
        unregisterComponent(index)
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
        (COMPONENT_KEY_MAPPING[name]?.instanceId ?: -1) != -1

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
        internal val COMPONENT_SYSTEM_MAPPING: DynArray<ComponentSystem<*>> = DynArray.of()

        internal fun clearSystems(type: ComponentType<*>) = COMPONENT_SYSTEM_MAPPING[type.aspectIndex]?.clearSystem()
        internal fun clearAllSystems() {
            COMPONENT_SYSTEM_MAPPING.forEach { it.clearSystem() }
        }

        @Suppress("UNCHECKED_CAST")
        operator fun <C : Component> get(type: ComponentType<C>): ComponentSystem<C> {
            return COMPONENT_SYSTEM_MAPPING[type.aspectIndex] as ComponentSystem<C>
        }

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

abstract class ComponentSubTypeSystem<C : Component, CC : C>(
    system: ComponentSystem<C>,
) : ComponentBuilder<CC>(system), SubComponentSystemAdapter<CC>  {

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
}


