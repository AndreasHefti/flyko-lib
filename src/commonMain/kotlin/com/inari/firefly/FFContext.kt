package com.inari.firefly

import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.api.*
import com.inari.firefly.core.component.*
import com.inari.firefly.core.system.*
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.game.world.World
import com.inari.firefly.game.world.WorldSystem
import com.inari.util.Consumer
import com.inari.util.Named
import com.inari.util.OpResult
import com.inari.util.Predicate
import com.inari.util.aspect.Aspect
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.event.AspectedEvent
import com.inari.util.event.AspectedEventListener
import com.inari.util.event.Event
import com.inari.util.event.IEventDispatcher
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField


object FFContext {

    @PublishedApi @JvmField internal val componentMaps: DynArray<ComponentMap<*>> = DynArray.of()
    @PublishedApi @JvmField internal val componentLoadDispatcher: DynArray<ComponentLoadDispatcher<*>> = DynArray.of()
    @JvmField internal val systemTypeMapping: DynArray<ComponentSystem> = DynArray.of()
    private val systemMapping = DynArray.of<ComponentSystem>()

    val eventDispatcher: IEventDispatcher
        get() = FFApp.eventDispatcher

    val graphics: GraphicsAPI
        get() = FFApp.graphics

    val audio: AudioAPI
        get() = FFApp.audio

    val input: InputAPI
        get() = FFApp.input

    val timer: TimerAPI
        get() = FFApp.timer

    val resourceService: ResourceServiceAPI
        get() = FFApp.resourceService

    val screenWidth: Int
        get() = graphics.screenWidth
    val screenHeight: Int
        get() = graphics.screenHeight

    fun <S : ComponentSystem> loadSystem(system: S) {
        systemMapping.add(system)
        for (aspect in system.supportedComponents)
            systemTypeMapping[aspect.aspectIndex] = system
    }

    // **** MAPPERS AND DISPATCHER *****

    fun <C : Component> mapper(compAspect: Aspect): ComponentMap<C>  =
        @Suppress("UNCHECKED_CAST")
        if (SystemComponent.SYSTEM_COMPONENT_ASPECTS.typeCheck(compAspect) &&
            componentMaps.contains(compAspect.aspectIndex))

            componentMaps[compAspect.aspectIndex] as ComponentMap<C>
        else throw RuntimeException("No Component Mapper registered for compAspect: $compAspect")

    fun <C : Component> mapper(id: CompId): ComponentMap<C> = mapper<C>(id.componentType)
    fun <C : Component> mapper(id: CompNameId): ComponentMap<C> = mapper<C>(id.componentType)
    fun <C : SystemComponent> mapper(coType: SystemComponentType<C>): ComponentMap<C> = mapper(coType.compAspect)
    @Suppress("UNCHECKED_CAST")
    fun <C : Component> mapper(coType: ComponentType<C>): ComponentMap<C> {
        val index = coType.aspectIndex
        if (!componentMaps.contains(index))
            throw RuntimeException("No Component Mapper registered for subType: $coType")
        return componentMaps[index] as ComponentMap<C>
    }

    @Suppress("UNCHECKED_CAST")
    fun <C: Component> loadDispatcher(coType: ComponentType<C>): ComponentLoadDispatcher<C> {
        val index = coType.aspectIndex
        if (!componentLoadDispatcher.contains(index))
            throw RuntimeException("No Component Load Dispatcher registered for subType: $coType")
        return componentLoadDispatcher[index] as ComponentLoadDispatcher<C>
    }

    // **** GET COMPONENT *****

    operator fun <C : Component> get(id: CompId): C = mapper<C>(id)[id.instanceId]
    operator fun <C : Component> get(id: CompNameId): C = mapper<C>(id)[id.name]
    operator fun <C : Component> get(cType: ComponentType<C>, index: Int): C = mapper(cType)[index]
    operator fun <C : Component> get(cType: ComponentType<C>, indexed: Indexed): C = mapper(cType)[indexed.index]
    operator fun <C : Component> get(cType: ComponentType<C>, name: String): C = mapper(cType)[name]
    operator fun <C : Component> get(cType: ComponentType<C>, named: Named): C = mapper(cType)[named.name]

    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, index: Int): CC = mapper<CC>(cType)[index]
    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, indexed: Indexed): CC = mapper<CC>(cType)[indexed.index]
    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, name: String): CC = mapper<CC>(cType)[name]
    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, named: Named): CC = mapper<CC>(cType)[named.name]

    operator fun <E : EntityComponent> get(ecType: EntityComponentType<E>, entityId: Int): E = EntitySystem.entities[entityId][ecType]
    operator fun <E : EntityComponent> get(ecType: EntityComponentType<E>, entityId: CompId): E = EntitySystem.entities[entityId][ecType]
    operator fun <E : EntityComponent> get(ecType: EntityComponentType<E>, entityName: String): E = EntitySystem.entities[entityName][ecType]
    operator fun <E : EntityComponent> get(ecType: EntityComponentType<E>, entityName: Named): E = EntitySystem.entities[entityName.name][ecType]

    // **** LOAD COMPONENT *****

    fun load(cType: ComponentType<*>, index: Int): FFContext {
        loadDispatcher(cType).loadDispatch(index)
        return this
    }
    fun load(cType: ComponentType<*>, name: String): FFContext {
        loadDispatcher(cType).loadDispatch(name)
        return this
    }
    fun load(component: Component) = load(component.componentId)
    fun load(cType: ComponentType<*>, indexed: Indexed) = load(cType, indexed.index)
    fun load(id: CompId) = load(id.componentType, id.index)
    fun loadAll(ids: DynArray<CompId>) = ids.forEach { load(it) }
    fun load(id: CompNameId) = load(id.componentType, id.name)
    fun load(cType: ComponentType<*>, named: Named) = load(cType, named.name)
    fun loadAllByNameId(ids: DynArray<CompNameId>) = ids.forEach { load(it) }
    fun load(singleton: SingletonComponent<*,*>) = load(singleton, singleton.instance.index)
    fun loadAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            load(cType, i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }

    // **** IS LOADED *****

    fun isLoaded(component: Component): Boolean = isLoaded(component.componentId.componentType, component.index)
    fun isLoaded(cType: ComponentType<*>, index: Int): Boolean = loadDispatcher(cType).isLoaded(index)
    fun isLoaded(cType: ComponentType<*>, indexed: Indexed): Boolean = isLoaded(cType, indexed.index)
    fun isLoaded(id: CompId): Boolean = isLoaded(id.componentType, id.index)
    fun isLoaded(id: CompNameId): Boolean = isLoaded(id.componentType, id.name)
    fun isLoaded(cType: ComponentType<*>, name: String): Boolean = loadDispatcher(cType).isLoaded(name)
    fun isLoaded(cType: ComponentType<*>, named: Named): Boolean = isLoaded(cType, named.name)
    fun isLoaded(singleton: SingletonComponent<*, *>): Boolean = isLoaded(singleton, singleton.aspectIndex)

    // **** ASSET INSTANCE *****

    fun assetInstanceId(assetId: Int): Int = AssetSystem.assets[assetId].instanceId
    fun assetInstanceId(assetName: String): Int = AssetSystem.assets[assetName].instanceId
    fun assetInstanceId(assetName: Named): Int = AssetSystem.assets[assetName.name].instanceId

    // **** ACTIVATE COMPONENT *****

    fun activate(cType: ComponentType<*>, index: Int): FFContext {
        mapper<Component>(cType).activate(index)
        return this
    }
    fun activate(cType: ComponentType<*>, name: String): FFContext {
        mapper<Component>(cType).activate(name)
        return this
    }

    fun activate(component: Component) = activate(component.componentId.componentType, component.index)
    fun activate(cType: ComponentType<*>, indexed: Indexed) = activate(cType, indexed.index)
    fun activate(id: CompId) = activate(id.componentType, id.index)
    fun activateAll(ids: DynArray<CompId>) = ids.forEach { activate(it) }
    fun activate(id: CompNameId) = activate(id.componentType, id.name)
    fun activate(cType: ComponentType<*>, named: Named) = activate(cType, named.name)
    fun activateAllByNameId(ids: DynArray<CompNameId>) = ids.forEach { activate(it) }
    fun activate(singleton: SingletonComponent<*,*>) = activate(singleton, singleton.instance.index)
    fun activateAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            activate(cType, i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }

    // **** EXISTS COMPONENT *****

    fun exists(component: Component): Boolean = exists(component.componentId.componentType, component.index)
    fun exists(cType: ComponentType<*>, index: Int): Boolean = mapper<Component>(cType).contains(index)
    fun exists(cType: ComponentType<*>, indexed: Indexed): Boolean = mapper<Component>(cType).contains(indexed.index)
    fun exists(id: CompId): Boolean = mapper<Component>(id).contains(id.instanceId)
    fun exists(id: CompNameId): Boolean = mapper<Component>(id).contains(id.name)
    fun exists(cType: ComponentType<*>, name: String): Boolean = mapper<Component>(cType).contains(name)
    fun exists(cType: ComponentType<*>, named: Named): Boolean = mapper<Component>(cType).contains(named.name)
    fun exists(singleton: SingletonComponent<*, *>): Boolean = mapper<Component>(singleton).contains(singleton.instance.index)

    // **** IS COMPONENT ACTIVATE *****

    fun isActive(component: Component): Boolean = isActive(component.componentId.componentType, component.index)
    fun isActive(cType: ComponentType<*>, index: Int): Boolean = mapper<Component>(cType).isActive(index)
    fun isActive(cType: ComponentType<*>, indexed: Indexed): Boolean = mapper<Component>(cType).isActive(indexed.index)
    fun isActive(id: CompId): Boolean = mapper<Component>(id).isActive(id.instanceId)
    fun isActive(id: CompNameId): Boolean = mapper<Component>(id).isActive(id.name)
    fun isActive(cType: ComponentType<*>, name: String): Boolean = mapper<Component>(cType).isActive(name)
    fun isActive(cType: ComponentType<*>, named: Named): Boolean = mapper<Component>(cType).isActive(named.name)
    fun isActive(singleton: SingletonComponent<*, *>): Boolean = mapper<Component>(singleton).isActive(singleton.instance.index)


    // **** DEACTIVATE COMPONENT *****

    fun deactivate(cType: ComponentType<*>, index: Int): FFContext {
        mapper<Component>(cType).deactivate(index)
        return this
    }
    fun deactivate(cType: ComponentType<*>, name: String): FFContext {
        mapper<Component>(cType).deactivate(name)
        return this
    }
    fun deactivate(component: Component) = deactivate(component.componentId)
    fun deactivate(cType: ComponentType<*>, indexed: Indexed) = deactivate(cType, indexed.index)
    fun deactivate(id: CompId) = deactivate(id.componentType, id.index)
    fun deactivate(id: CompNameId) = deactivate(id.componentType, id.name)
    fun deactivate(cType: ComponentType<*>, named: Named) = deactivate(cType, named.name)
    fun deactivate(singleton: SingletonComponent<*,*>) = deactivate(singleton, singleton.instance.index)
    fun deactivateAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            mapper<Component>(cType).deactivate(i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }
    fun deactivateAll(list: DynArray<CompId>): FFContext {
        val it = list.iterator()
        while (it.hasNext())
            deactivate(it.next())
        return this
    }

    // **** DISPOSE COMPONENT *****

    fun dispose(cType: ComponentType<*>, index: Int): FFContext {
        loadDispatcher(cType).disposeDispatch(index)
        return this
    }
    fun dispose(cType: ComponentType<*>, name: String): FFContext {
        loadDispatcher(cType).disposeDispatch(name)
        return this
    }
    fun dispose(component: Component) = dispose(component.componentId)
    fun dispose(cType: ComponentType<*>, indexed: Indexed) = dispose(cType, indexed.index)
    fun dispose(id: CompId) = dispose(id.componentType, id.index)
    fun disposeAll(ids: DynArray<CompId>) = ids.forEach { dispose(it) }
    fun dispose(id: CompNameId) = dispose(id.componentType, id.name)
    fun dispose(cType: ComponentType<*>, named: Named) = dispose(cType, named.name)
    fun disposeAllByNameId(ids: DynArray<CompNameId>) = ids.forEach { dispose(it) }
    fun dispose(singleton: SingletonComponent<*,*>) = dispose(singleton, singleton.instance.index)
    fun disposeAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            dispose(cType, i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }

    // **** DELETE COMPONENT *****

    fun delete(cType: ComponentType<*>, index: Int): FFContext {
        val mapper = mapper<Component>(cType)
        if (mapper.map.contains(index))
            mapper.delete(index)
        else
            println("WARN: Component not found, abort delete: $cType - $index")
        return this
    }
    fun delete(cType: ComponentType<*>, name: String): FFContext {
        if (name == NO_NAME)
            return this
        val mapper = mapper<Component>(cType)
        if (mapper.contains(name))
            mapper.delete(name)
        else
            println("WARN: Component not found, abort delete: $cType - $name")
        return this
    }

    fun delete(component: Component) = delete(component.componentId.componentType, component.index)
    fun delete(cType: ComponentType<*>, indexed: Indexed) = delete(cType, indexed.index)
    fun delete(id: CompId) = delete(id.componentType, id.instanceId)
    fun delete(id: CompNameId) = delete(id.componentType, id.name)
    fun delete(cType: ComponentType<*>, named: Named) = delete(cType, named.name)
    fun delete(singleton: SingletonComponent<*, *>) = delete(singleton, singleton.instance.index)
    fun deleteAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            delete(cType, i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }
    fun deleteAll(list: DynArray<CompId>): FFContext {
        val it = list.iterator()
        while (it.hasNext())
            delete(it.next())
        return this
    }

    // **** LISTENERS *****

    fun <L> registerListener(event: Event<L>, listener: L): FFContext {
        eventDispatcher.register(event.eventType, listener)
        return this
    }

    fun <L> registerListener(eventType: Event.EventType, listener: L): FFContext {
        eventDispatcher.register(eventType, listener)
        return this
    }

    fun <L> disposeListener(event: Event<L>, listener: L): FFContext {
        eventDispatcher.unregister(event.eventType, listener)
        return this
    }

    fun <L> disposeListener(eventType: Event.EventType, listener: L): FFContext {
        eventDispatcher.unregister(eventType, listener)
        return this
    }

    fun <L> notify(event: Event<L>): FFContext {
        eventDispatcher.notify(event)
        return this
    }

    fun <L : AspectedEventListener> notify(event: AspectedEvent<L>): FFContext {
        eventDispatcher.notify(event)
        return this
    }

    // **** MISC *****

    fun <C : Component> forEach(cType: ComponentType<*>, consumer: Consumer<C>) {
        val mapper = mapper<C>(cType)
        mapper.forEach(consumer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <C : SystemComponent, CC : C> forEach(cType: SystemComponentSubType<C, CC>, consumer: Consumer<CC>) {
        val mapper = mapper(cType)
        mapper.forEach { c ->
            if (c.componentType() == cType)
                consumer(c as CC)
        }
    }

    fun <C : Component> forEach(cType: ComponentType<C>, components: BitSet, consumer: Consumer<C>) {
        var index = components.nextSetBit(0)
        while (index >= 0) {
            consumer(FFContext[cType, index])
            index = components.nextSetBit(index + 1)
        }
    }

    fun <C : SystemComponent, CC : C> forEach(cType: SystemComponentSubType<C, CC>, components: BitSet, consumer: Consumer<CC>) {
        var index = components.nextSetBit(0)
        while (index >= 0) {
            consumer(FFContext[cType, index])
            index = components.nextSetBit(index + 1)
        }
    }

    fun <C : SystemComponent, CC : C> findFirst(cType: SystemComponentSubType<C, CC>, components: BitSet, predicate: Predicate<CC>) : CC? {
        var index = components.nextSetBit(0)
        while (index >= 0) {
            val c = FFContext[cType, index]
            if (predicate(c))
                return c
            index = components.nextSetBit(index + 1)
        }
        return null
    }

    fun runTask(
        name: String,
        compId1: CompId = NO_COMP_ID,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID): OpResult = TaskSystem.tasks[name](compId1, compId2, compId3)

    fun runTask(
        taskId: CompId,
        compId1: CompId = NO_COMP_ID,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID): OpResult = TaskSystem.tasks[taskId](compId1, compId2, compId3)

    fun runTask(
        taskIndex: Int,
        compId1: CompId = NO_COMP_ID,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID): OpResult = TaskSystem.tasks[taskIndex](compId1, compId2, compId3)


    fun loadShaderProgram(resource: String): String {
        val shaderProgram = resourceService.loadTextResource(resource)
            .lines()
            .map {
                if (it.startsWith( "#pragma flyko-lib: import") )
                    loadShaderProgram(it.substring(it.indexOf("=") + 1).trim())
                else it
            }.reduce{ acc, s ->
                acc + "\n" + s
            }
        return shaderProgram
    }


    fun dump(full: Boolean = false): String {
        val builder = StringBuilder()
        builder.append("FFContext: {")

        builder.append("\n  Systems: ")
        systemMapping.forEachIndexed() { i, system ->

            system.supportedComponents.forEach { aspect ->
                builder.append("\n    ").append(system::class.simpleName!!)
                    .append(":").append(aspect.aspectName)
                    .append(":").append(aspect.aspectIndex)
                    .append(":").append(i)
            }
        }

        builder.append("\n  Components : ")
        componentMaps.forEach { cMap ->
            builder.append("\n    ").append(cMap.componentType.aspectName)
                .append(":").append(cMap.map.size)
            if (full) {
                cMap.map.forEach { comp ->
                    builder.append("\n      ").append(comp.index)

                    if (comp is NamedComponent)
                        builder.append(" -- name: ").append(comp.name)

                    if (comp is SystemComponent)
                        builder.append(" -- subtype: ").append(comp.componentType().subTypeClass.simpleName)
                }
            }
        }
        builder.append("\n}")
        return builder.toString()
    }

}