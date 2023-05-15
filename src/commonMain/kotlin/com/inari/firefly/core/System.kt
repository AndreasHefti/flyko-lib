package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.core.Component.Companion.COMPONENT_GROUP_ASPECT
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.NO_NAME
import com.inari.util.TRUE_PREDICATE
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.collection.*
import com.inari.util.event.Event

interface System {
    fun clearSystem()
}

interface IComponentSystem<C : Component> : ComponentBuilder<C>, ComponentType<C>, System, Iterable<C> {
    val systemName: String
    val hasComponents: Boolean
    val hasActiveComponents: Boolean

    fun setMinCapacity(c: Int)

    fun nextIndex(from: Int): ComponentIndex
    fun nextActiveIndex(from: Int): ComponentIndex
    fun indexIterator(): IndexIterator
    fun activeIndexIterator(): IndexIterator

    fun createKey(name: String): ComponentKey
    fun hasKey(name: String): Boolean
    fun getOrCreateKey(name: String): ComponentKey
    /** Returns NO_COMPONENT_KEY if key doesn't exist */
    fun getKey(name: String): ComponentKey
    /** Returns NO_COMPONENT_KEY if key doesn't exist */
    fun getKey(index: ComponentIndex): ComponentKey

    operator fun get(name: String): C {
        val keyForName = getKey(name)
        if (keyForName == NO_COMPONENT_KEY) throw IllegalArgumentException("No component for name: $name found on system: $systemName")
        checkKey(keyForName)
        return get(keyForName.componentIndex)
    }
    operator fun get(ref: CReference): C  = get(checkKey(ref.targetKey).componentIndex)
    operator fun get(key: ComponentKey): C  = get(checkKey(key).componentIndex)
    operator fun get(index: ComponentIndex): C

    fun exists(name: String): Boolean = getKey(name).componentIndex > NULL_COMPONENT_INDEX
    fun exists(key: ComponentKey): Boolean = exists(checkKey(key).componentIndex)
    fun exists(index: ComponentIndex): Boolean

    fun isActive(name: String): Boolean = isActive(getKey(name))
    fun isActive(key: ComponentKey): Boolean = key.componentIndex > NULL_COMPONENT_INDEX && isActive(key.componentIndex)
    fun isActive(index: ComponentIndex): Boolean

    fun load(c: C) = load(c.index)
    fun load(cRef: CReference) = load(cRef.targetKey)
    fun load(name: String) = load(getKey(name).componentIndex)
    fun load(key: ComponentKey) = load(key.componentIndex)
    fun load(index: ComponentIndex)
    fun loadGroup(group: String) = loadGroup(COMPONENT_GROUP_ASPECT[group]!!)
    fun loadGroup(group: Aspect)

    fun activate(c: C) = activate(c.index)
    fun activate(cRef: CReference) = activate(cRef.targetKey)
    fun activate(name: String) = activate(getKey(name).componentIndex)
    fun activate(key: ComponentKey) = activate(key.componentIndex)
    fun activate(index: ComponentIndex)
    fun activateGroup(group: String) = activateGroup(COMPONENT_GROUP_ASPECT[group]!!)
    fun activateGroup(group: Aspect)

    fun deactivate(c: C) = deactivate(c.index)
    fun deactivate(cRef: CReference) = deactivate(cRef.targetKey)
    fun deactivate(name: String) = deactivate(getKey(name).componentIndex)
    fun deactivate(key: ComponentKey) = deactivate(key.componentIndex)
    fun deactivate(index: ComponentIndex)
    fun deactivateGroup(group: String) = deactivateGroup(COMPONENT_GROUP_ASPECT[group]!!)
    fun deactivateGroup(group: Aspect)

    fun dispose(c: C) = dispose(c.index)
    fun dispose(cRef: CReference) = dispose(cRef.targetKey)
    fun dispose(name: String) = dispose(getKey(name).componentIndex)
    fun dispose(key: ComponentKey) = dispose(key.componentIndex)
    fun dispose(index: Int)
    fun disposeGroup(group: String) = disposeGroup(COMPONENT_GROUP_ASPECT[group]!!)
    fun disposeGroup(group: Aspect)

    fun delete(c: C) = delete(c.index)
    fun delete(cRef: CReference) = delete(cRef.targetKey)
    fun delete(name: String) = delete(getKey(name).componentIndex)
    fun delete(key: ComponentKey) = delete(checkKey(key).componentIndex)
    fun delete(index: ComponentIndex)
    fun deleteGroup(group: String) = deleteGroup(COMPONENT_GROUP_ASPECT[group]!!)
    fun deleteGroup(group: Aspect)

    val componentEventType: Event.EventType
    fun registerComponentListener(listener: ComponentEventListener) =
        Engine.registerListener(componentEventType, listener)
    fun disposeComponentListener(listener: ComponentEventListener) =
        Engine.disposeListener(componentEventType, listener)

    fun forEachDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        val iter = iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (filter(c))
                process(c)
        }
    }

    fun forEachActiveDo(
        filter: (C) -> Boolean = TRUE_PREDICATE,
        process: (C) -> Unit
    ) {
        val iter = activeIndexIterator()
        while (iter.hasNext()) {
            val c = this[iter.nextInt()]
            if (!c.active) continue
            if (filter(c))
                process(c)
        }
    }

    private fun checkKey(key: ComponentKey): ComponentKey =
        if (this.aspectIndex != key.type.aspectIndex)
            throw IllegalArgumentException("Component type mismatch: ${this.aspectName} | ${key.type.aspectName} ")
        else key
}

@Suppress("LeakingThis")
abstract class ComponentSystem<C : Component>(
    final override val systemName: String
) : IComponentSystem<C> {

    private var typeAspect: Aspect = Component.COMPONENT_TYPE_ASPECTS.createAspect(systemName)

    final override val typeName: String = systemName
    final override val subTypeName: String = systemName
    final override val aspectIndex: Int = typeAspect.aspectIndex
    final override val aspectName: String = typeAspect.aspectName
    final override val aspectType: AspectType = typeAspect.aspectType

    // TODO private and adapt in SubType
    internal val _componentKeyMapping: MutableMap<String, ComponentKey> =  HashMap()
    val componentKeyMapping: Map<String, ComponentKey> = _componentKeyMapping
    private val _componentMapping: DynArray<C> = DynArray(50, 100) { size -> allocateArray(size) }
    val componentMapping: DynArrayRO<C> = _componentMapping
    private val _activeComponentSet: BitSet = BitSet()
    val activeComponentSet: IndexIterable = _activeComponentSet
    private val _eventPool = ArrayDeque<ComponentEvent>()

    init { registerSystem(this) }

    // **** MISC ****
    // **************
    internal abstract fun allocateArray(size: Int): Array<C?>

    override val hasComponents: Boolean
        get() = !_componentMapping.isEmpty
    override val hasActiveComponents: Boolean
        get() = !_activeComponentSet.isEmpty

    override fun setMinCapacity(c: Int) = _componentMapping.ensureCapacity(c)

    override fun toString(): String =
        "System: $typeName" +
        "\n  Key Mapping: ${_componentKeyMapping.values}\n" +
        "  Component Mapping: ${mappingToString()}\n"

    fun mappingToString(): String {
        val builder = StringBuilder()
        val iter = iterator()
        while (iter.hasNext())
            builder.append("\n    ${iter.next()}")
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
        configure(comp)
        val key = registerComponent(comp)
        initComponent(comp)
        return key
    }

    override fun buildAndGet(configure: C.() -> Unit): C {
        val comp: C = create()
        configure(comp)
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

        var index = _componentMapping.nextIndex(0)
        while (index > NULL_COMPONENT_INDEX) {
            this.internalDelete(index)
            index = _componentMapping.nextIndex(index + 1)
        }

        val iter = _componentKeyMapping.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (entry.value.componentIndex <= NULL_COMPONENT_INDEX)
                _componentKeyMapping.remove(entry.key)
        }

        _eventPool.clear()
    }

    // **** Component Handling ****
    // ****************************
    internal open fun registerComponent(c: C): ComponentKey {
        // check name clash
        if (c.name != NO_NAME && c.name in _componentKeyMapping) {
            val key = _componentKeyMapping[c.name]!!
            if (key.componentIndex >= 0 && key.componentIndex != c.index)
                throw IllegalArgumentException("Key with same name already exists. Name: ${c.name}")
        }

        _componentMapping[c.index] = c

        val key = when {
            // If the component has no name, an ad-hoc ComponentRef is being created but not stored on the name mapping
            (c.name == NO_NAME) -> ComponentKey(NO_NAME, c.componentType)
            // get existing key and update with component index
            (c.name in _componentKeyMapping) -> _componentKeyMapping[c.name]!!
            // create new key and store it in the name mapping
            else -> {
                val key = ComponentKey(c.name, c.componentType)
                _componentKeyMapping[c.name] = key
                key
            }
        }

        key.componentIndex = c.index
        c.key = key
        return key
    }

    fun registerStatic(component: C) {
        if (component.name == NO_NAME)
            component.name = component::class.simpleName!!

        component.onStateChange = true
        component.survivesSystemClear = true
        registerComponent(component)
        component.iInitialize()
        component.initialized = true
        component.onStateChange = false
        send(component.key, ComponentEventType.INITIALIZED)
        if (component.autoActivation)
            activate(component.index)
    }

    internal open fun unregisterComponent(index: ComponentIndex) {
        val removed = _componentMapping.remove(index)
        if (removed != null && removed.name != NO_NAME)
            _componentKeyMapping.remove(removed.name)?.clearKey()
    }

    override fun nextIndex(from: ComponentIndex) = _componentMapping.nextIndex(from)
    override fun nextActiveIndex(from: ComponentIndex) = _activeComponentSet.nextIndex(from)
    override fun indexIterator() = IndexIterator(_componentMapping)
    override fun activeIndexIterator() = IndexIterator(_activeComponentSet)
    override fun iterator(): Iterator<C> = IndexedTypeIterator(_componentMapping)

    override operator fun get(index: ComponentIndex): C = _componentMapping[index]
        ?: throw IllegalArgumentException("No component for index: $index on system: $typeName")

    override fun exists(index: ComponentIndex): Boolean = _componentMapping.contains(index)
    override fun isActive(index: ComponentIndex): Boolean = _activeComponentSet[index]
    override fun load(index: ComponentIndex) {

        // DEBUG  println("--> load: ${aspectName}:${index}")

        if (!checkIndex(index)) return
        val comp =  this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to load")
        if (comp.onStateChange || comp.loaded) return

        comp.onStateChange = true
        comp.iLoad()
        comp.loaded = true
        comp.onStateChange = false
        send(comp.key, ComponentEventType.LOADED)

        // DEBUG  println("<-- load: ${aspectName}:${index}")
    }

    override fun loadGroup(group: Aspect) {
        val iter = iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (group in c.groups)
                load(c)
        }
    }

    override fun activate(index: ComponentIndex) {

        // DEBUG  println("--> activate: ${aspectName}:${index}")

        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to activate")
        if (comp.onStateChange || comp.active) return

        if (!comp.loaded) // load first before activation
            load(index)

        comp.onStateChange = true
        comp.iActivate()
        comp.active = true
        comp.onStateChange = false
        _activeComponentSet[index] = true
        send(comp.key, ComponentEventType.ACTIVATED)

        // DEBUG  println("<-- activate: ${aspectName}:${index}")
    }

    override fun activateGroup(group: Aspect) {
        val iter = iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (group in c.groups)
                activate(c)
        }
    }

    override fun deactivate(index: ComponentIndex) {

        // DEBUG  println("--> deactivate: ${aspectName}:${index}")

        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to activate")
        if (comp.onStateChange || !comp.active) return

        comp.onStateChange = true
        comp.iDeactivate()
        comp.active = false
        comp.onStateChange = false
        _activeComponentSet[index] = false
        send(comp.key, ComponentEventType.DEACTIVATED)

        // DEBUG  println("<-- deactivate: ${aspectName}:${index}")
    }

    override fun deactivateGroup(group: Aspect) {
        val iter = iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (group in c.groups)
                deactivate(c)
        }
    }

    override fun dispose(index: ComponentIndex) {

        // DEBUG  println("--> dispose: ${aspectName}:${index}")

        if (!checkIndex(index)) return
        val comp = this[index]
        if (!comp.initialized) throw IllegalStateException("Component in illegal state to dispose")
        if (comp.onStateChange || !comp.loaded) return

        if (comp.active) // deactivate first if still active
            deactivate(index)

        comp.onStateChange = true
        comp.iDispose()
        comp.loaded = false
        comp.onStateChange = false
        send(comp.key, ComponentEventType.DISPOSED)

        // DEBUG  println("<-- dispose: ${aspectName}:${index}")
    }

    override fun disposeGroup(group: Aspect) {
        val iter = iterator()
        while (iter.hasNext()) {
            val c = iter.next()
            if (group in c.groups)
                dispose(c)
        }
    }

    override fun delete(index: ComponentIndex) = internalDelete(index)
    internal fun internalDelete(index: ComponentIndex) {

        // DEBUG  println("--> delete: ${aspectName}:${index}")

        if (!checkIndex(index)) return
        val comp = this[index]
        if (comp.onStateChange || comp.survivesSystemClear) return

        if (comp.loaded) // dispose first when still loaded
            dispose(index)

        comp.onStateChange = true
        comp.iDelete()
        comp.initialized = false
        comp.onStateChange = false
        send(comp.key, ComponentEventType.DELETED)
        unregisterComponent(index)
        comp.iDisposeIndex()

        // DEBUG  println("<-- delete: ${aspectName}:${index}")
    }

    override fun deleteGroup(group: Aspect) {
        val index = _componentMapping.nextIndex(0)
        while (index > NULL_COMPONENT_INDEX) {
            val c = _componentMapping[index]!!
            if (group in c.groups)
                dispose(c)
        }
    }

    fun checkIndex(index: ComponentIndex): Boolean  {
        if (index < 0)
            println("!!! No component instance defined (index < 0) $typeName - $subTypeName. Ignore it !!!")
        return (index >= 0)
    }

    // **** ComponentKey handling ****
    // *******************************
    override fun createKey(name: String): ComponentKey {
        if (name == NO_NAME)
            throw IllegalArgumentException("Key with no name")
        if (name in _componentKeyMapping)
            throw IllegalArgumentException("Key with same name already exists")
        val newKey = ComponentKey(name, this)
        _componentKeyMapping[name] = newKey
        return newKey
    }
    override fun hasKey(name: String): Boolean = name in _componentKeyMapping

    override fun getOrCreateKey(name: String): ComponentKey = getOrCreateKey(name, this)
    internal fun getOrCreateKey(name: String, system: IComponentSystem<*>): ComponentKey =
        if (hasKey(name))
            _componentKeyMapping[name]!!
        else
            system.createKey(name)

    override fun getKey(name: String): ComponentKey =
        if (hasKey(name))
            _componentKeyMapping[name]!!
        else
            NO_COMPONENT_KEY

    override fun getKey(index: ComponentIndex): ComponentKey =
        _componentMapping[index]?.key ?: NO_COMPONENT_KEY

    // **** Event Handling ****
    // ************************
    override val componentEventType: Event.EventType = Event.EventType("$typeName Event")
    internal fun send(key: ComponentKey, eventType: ComponentEventType) {
        val event = if (!_eventPool.isEmpty())
            _eventPool.removeFirst()
        else
            ComponentEvent(componentEventType)
        event.key = key
        event.componentEventType = eventType
        Engine.notify(event)
        event.key = NO_COMPONENT_KEY
        _eventPool.addFirst(event)
    }

    override fun registerComponentListener(listener: ComponentEventListener) =
        Engine.registerListener(componentEventType, listener)
    override fun disposeComponentListener(listener: ComponentEventListener) =
        Engine.disposeListener(componentEventType, listener)

    companion object {

        private val COMPONENT_SYSTEM_MAPPING = mutableMapOf<String, IComponentSystem<*>>()
        internal fun registerSystem(system: IComponentSystem<*>) {
            if (system.subTypeName in COMPONENT_SYSTEM_MAPPING)
                throw IllegalArgumentException("Subsystem of type: ${system.systemName} is already registered and is not of type AbstractSubTypeSystem")

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

        fun loadAllOfGroup(group: String) = loadAllOfGroup(COMPONENT_GROUP_ASPECT[group]!!)
        fun loadAllOfGroup(group: Aspect) {
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                iter.next().loadGroup(group)
        }

        fun activateAllOfGroup(group: String) = activateAllOfGroup(COMPONENT_GROUP_ASPECT[group]!!)
        fun activateAllOfGroup(group: Aspect) {
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                iter.next().activateGroup(group)
        }

        fun deactivateAllOfGroup(group: String) = deactivateAllOfGroup(COMPONENT_GROUP_ASPECT[group]!!)
        fun deactivateAllOfGroup(group: Aspect) {
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                iter.next().deactivateGroup(group)
        }

        fun disposeAllOfGroup(group: String) = disposeAllOfGroup(COMPONENT_GROUP_ASPECT[group]!!)
        fun disposeAllOfGroup(group: Aspect) {
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                iter.next().disposeGroup(group)
        }

        fun deleteAllOfGroup(group: String) = deleteAllOfGroup(COMPONENT_GROUP_ASPECT[group]!!)
        fun deleteAllOfGroup(group: Aspect) {
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                iter.next().deleteGroup(group)
        }

        fun clearSystems() {
            val iter = HashMap(COMPONENT_SYSTEM_MAPPING).values.iterator()
            while (iter.hasNext())
                iter.next().clearSystem()
        }

        fun dumpInfo() {
            println("---- System Info ---------------------------------------------------------------------------------")
            val iter = COMPONENT_SYSTEM_MAPPING.values.iterator()
            while (iter.hasNext())
                println(iter.next())
            println("--------------------------------------------------------------------------------------------------")
        }
    }
}

class ComponentEvent internal constructor(override val eventType: EventType): Event<ComponentEventListener>() {
    internal var key: ComponentKey = NO_COMPONENT_KEY
    internal lateinit var componentEventType: ComponentEventType
    override fun notify(listener: ComponentEventListener) = listener(key, componentEventType)
}

abstract class AbstractComponentSystem<C : Component>(systemName: String) : ComponentSystem<C>(systemName) {
    override fun create(): C = throw UnsupportedOperationException("$systemName is abstract use sub type builder instead")
}

abstract class  SubComponentBuilder<C : Component, CC : C>(
    val system: ComponentSystem<C>
) :  ComponentBuilder<CC>  {
    override fun build(configure: CC.() -> Unit): ComponentKey {
        val comp: CC = create()
        configure(comp)
        val key = system.registerComponent(comp)
        system.initComponent(comp)
        return key
    }
    override fun buildAndGet(configure: CC.() -> Unit): CC {
        val comp: CC = create()
        configure(comp)
        system.registerComponent(comp)
        system.initComponent(comp)
        return comp
    }
    abstract fun create(): CC
}

abstract class ComponentSubTypeBuilder<C : Component, CC : C>(
     val system: ComponentSystem<C>,
     final override val subTypeName: String
 ) : IComponentSystem<CC> {

    private val subComponentRefs = BitSet()
    private val activeSubComponentRefs = BitSet()

    final override val systemName: String = subTypeName
    final override val typeName: String = system.typeName
    final override val aspectIndex: Int = system.aspectIndex
    final override val aspectName: String = system.aspectName
    final override val aspectType: AspectType = system.aspectType

    override val componentEventType: Event.EventType = system.componentEventType
    override val hasComponents: Boolean
        get() = !subComponentRefs.isEmpty
    override val hasActiveComponents: Boolean
        get() = activeSubComponentRefs.nextIndex(0) >= 0

    override fun setMinCapacity(c: Int) = system.setMinCapacity(c)

    override fun nextIndex(from: ComponentIndex) = subComponentRefs.nextIndex(from)
    override fun nextActiveIndex(from: ComponentIndex) = activeSubComponentRefs.nextIndex(from)
    override fun indexIterator() = IndexIterator(subComponentRefs)
    override fun activeIndexIterator() = IndexIterator(activeSubComponentRefs)
    override fun iterator(): Iterator<CC> = IndexedTypeIterator(iterableTypeAdapter)
    private val iterableTypeAdapter: IndexedTypeIterable<CC> = object : IndexedTypeIterable<CC> {
        override fun get(index: Int): CC? = if (subComponentRefs[index]) this@ComponentSubTypeBuilder[index] else null
        override fun nextIndex(from: Int): Int = subComponentRefs.nextIndex(from)
    }

    init {
        @Suppress("LeakingThis")
        ComponentSystem.registerSystem(this)
    }

    override fun clearSystem() {
        val iter = IndexIterator(subComponentRefs)
        while (iter.hasNext())
            delete(iter.nextInt())
    }

    override fun createKey(name: String): ComponentKey {
        if (name == NO_NAME)
            throw IllegalArgumentException("Key with no name")
        if (name in system.componentKeyMapping)
            throw IllegalArgumentException("Key with same name already exists")
        val newKey = ComponentKey(name, this)
        system._componentKeyMapping[name] = newKey
        return newKey
    }
    override fun hasKey(name: String) = system.hasKey(name)
    override fun getOrCreateKey(name: String) = system.getOrCreateKey(name, this)
    override fun getKey(name: String) = system.getKey(name)
    override fun getKey(index: ComponentIndex) = system.getKey(index)
    @Suppress("UNCHECKED_CAST")
    override fun get(index: ComponentIndex): CC = system[index] as CC
    override fun exists(index: ComponentIndex) = system.exists(index)
    override fun isActive(index: ComponentIndex): Boolean = activeSubComponentRefs[index]
    override fun load(index: ComponentIndex) = system.load(index)
    override fun loadGroup(group: Aspect) = system.loadGroup(group)
    override fun activate(index: ComponentIndex) {
        system.activate(index)
        activeSubComponentRefs[index] = true
    }
    override fun activateGroup(group: Aspect) = system.activateGroup(group)
    override fun deactivate(index: ComponentIndex) {
        system.deactivate(index)
        activeSubComponentRefs[index] = false
    }
    override fun deactivateGroup(group: Aspect) = system.deactivateGroup(group)
    override fun dispose(index: ComponentIndex) = system.dispose(index)
    override fun disposeGroup(group: Aspect) = system.disposeGroup(group)
    override fun delete(index: ComponentIndex)  {
        if (system.exists(index))
            system.internalDelete(index)
        else
            println("**** WARING: Component $aspectName $subTypeName $index has already been deleted")
        subComponentRefs[index] = false
    }
    override fun deleteGroup(group: Aspect) = system.deleteGroup(group)
    override fun build(configure: CC.() -> Unit): ComponentKey {
        val comp: CC = create()
        configure(comp)
        val key = system.registerComponent(comp)
        system.initComponent(comp)
        subComponentRefs.set(key.componentIndex)
        return key
    }
    override fun buildAndGet(configure: CC.() -> Unit): CC {
        val comp: CC = create()
        configure(comp)
        system.registerComponent(comp)
        subComponentRefs.set(comp.index)
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
