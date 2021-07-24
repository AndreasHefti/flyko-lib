package com.inari.firefly

import com.inari.firefly.asset.AssetSystem
import com.inari.firefly.core.api.*
import com.inari.firefly.core.component.*
import com.inari.firefly.core.system.*
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.EntitySystem
import com.inari.util.Named
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
    @JvmField  internal val systemTypeMapping: DynArray<ComponentSystem> = DynArray.of()

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

    val screenWidth: Int
        get() = graphics.screenWidth
    val screenHeight: Int
        get() = graphics.screenHeight

    fun <S : ComponentSystem> loadSystem(system: S) {
        for (aspect in system.supportedComponents)
            systemTypeMapping[aspect.aspectIndex] = system
    }

    fun <C : Component> mapper(compAspect: Aspect): ComponentMap<C>  =
        @Suppress("UNCHECKED_CAST")
        if (SystemComponent.SYSTEM_COMPONENT_ASPECTS.typeCheck(compAspect) &&
            componentMaps.contains(compAspect.aspectIndex))

            componentMaps[compAspect.aspectIndex] as ComponentMap<C>
        else throw RuntimeException("No Component Mapper registered for compAspect: $compAspect")


    fun <C : Component> mapper(id: CompId): ComponentMap<C> =
        mapper<C>(id.componentType)

    fun <C : Component> mapper(id: CompNameId): ComponentMap<C> =
        mapper<C>(id.componentType)

    fun <C : SystemComponent> mapper(coType: SystemComponentType<C>): ComponentMap<C> =
        mapper(coType.compAspect)

    @Suppress("UNCHECKED_CAST")
    fun <C : Component> mapper(coType: ComponentType<C>): ComponentMap<C> {
        val index = coType.aspectIndex
        if (!componentMaps.contains(index))
            throw RuntimeException("No Component Mapper registered for subType: $coType")
        return componentMaps[index] as ComponentMap<C>
    }

    operator fun <C : Component> get(id: CompId): C =
        mapper<C>(id)[id.instanceId]

    operator fun <C : Component> get(id: CompNameId): C =
        mapper<C>(id)[id.name]

    operator fun <C : Component> get(cType: ComponentType<C>, index: Int): C =
        mapper(cType)[index]


    operator fun <C : Component> get(cType: ComponentType<C>, indexed: Indexed): C =
        mapper(cType)[indexed.index]

    operator fun <C : Component> get(cType: ComponentType<C>, name: String): C =
        mapper(cType)[name]

    operator fun <C : Component> get(cType: ComponentType<C>, named: Named): C =
        mapper(cType)[named.name]

    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, index: Int): CC =
        mapper<CC>(cType)[index]

    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, indexed: Indexed): CC =
        mapper<CC>(cType)[indexed.index]

    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, name: String): CC =
        mapper<CC>(cType)[name]

    operator fun <C : SystemComponent, CC : C> get(cType: SystemComponentSubType<C, CC>, named: Named): CC =
        mapper<CC>(cType)[named.name]

    fun assetInstanceId(assetId: Int): Int =
        AssetSystem.assets[assetId].instanceId

    fun assetInstanceId(assetName: String): Int =
        AssetSystem.assets[assetName].instanceId

    fun assetInstanceId(assetName: Named): Int =
        AssetSystem.assets[assetName.name].instanceId

    operator fun <E : EntityComponent> get(entityId: Int, ecType: EntityComponentType<E>): E =
        EntitySystem.entities[entityId][ecType]

    operator fun <E : EntityComponent> get(entityId: CompId, ecType: EntityComponentType<E>): E =
        EntitySystem.entities[entityId][ecType]

    operator fun <E : EntityComponent> get(entityName: String, ecType: EntityComponentType<E>): E =
        EntitySystem.entities[entityName][ecType]

    operator fun <E : EntityComponent> get(entityName: Named, ecType: EntityComponentType<E>): E =
        EntitySystem.entities[entityName.name][ecType]

    fun isActive(component: Component): Boolean =
        isActive(component.componentId)

    fun isActive(cType: ComponentType<*>, index: Int): Boolean =
        mapper<Component>(cType).isActive(index)

    fun isActive(cType: ComponentType<*>, indexed: Indexed): Boolean =
        mapper<Component>(cType).isActive(indexed.index)

    fun isActive(id: CompId): Boolean =
            mapper<Component>(id).isActive(id.instanceId)

    fun isActive(id: CompNameId): Boolean =
        mapper<Component>(id).isActive(id.name)

    fun isActive(cType: ComponentType<*>, name: String): Boolean =
        mapper<Component>(cType).isActive(name)

    fun isActive(cType: ComponentType<*>, named: Named): Boolean =
        mapper<Component>(cType).isActive(named.name)

    fun isActive(singleton: SingletonComponent<*, *>): Boolean =
        mapper<Component>(singleton).isActive(singleton.instance.index)

    fun activate(component: Component): FFContext {
        activate(component.componentId)
        return this
    }

    fun activate(cType: ComponentType<*>, index: Int): FFContext {
        mapper<Component>(cType).activate(index)
        return this
    }

    fun activate(cType: ComponentType<*>, indexed: Indexed): FFContext {
        mapper<Component>(cType).activate(indexed.index)
        return this
    }

    fun activate(id: CompId): FFContext {
        mapper<Component>(id).activate(id.instanceId)
        return this
    }

    fun activate(id: CompNameId): FFContext {
        mapper<Component>(id).activate(id.name)
        return this
    }

    fun activate(cType: ComponentType<*>, name: String): FFContext {
        mapper<Component>(cType).activate(name)
        return this
    }

    fun activate(cType: ComponentType<*>, named: Named): FFContext {
        mapper<Component>(cType).activate(named.name)
        return this
    }

    fun activate(singleton: SingletonComponent<*,*>): FFContext {
        mapper<Component>(singleton).activate(singleton.instance.index)
        return this
    }

    fun deactivate(component: Component): FFContext {
        deactivate(component.componentId)
        return this
    }

    fun deactivate(cType: ComponentType<*>, index: Int): FFContext {
        mapper<Component>(cType).deactivate(index)
        return this
    }

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

    fun deactivate(cType: ComponentType<*>, indexed: Indexed): FFContext {
        mapper<Component>(cType).deactivate(indexed.index)
        return this
    }

    fun deactivate(id: CompId): FFContext {
        mapper<Component>(id).deactivate(id.instanceId)
        return this
    }

    fun deactivate(id: CompNameId): FFContext {
        mapper<Component>(id).deactivate(id.name)
        return this
    }

    fun deactivate(cType: ComponentType<*>, name: String): FFContext {
        mapper<Component>(cType).deactivate(name)
        return this
    }

    fun deactivate(cType: ComponentType<*>, named: Named): FFContext {
        mapper<Component>(cType).deactivate(named.name)
        return this
    }

    fun deactivate(singleton: SingletonComponent<*,*>): FFContext {
        mapper<Component>(singleton).deactivate(singleton.instance.index)
        return this
    }

    fun delete(component: Component): FFContext {
        delete(component.componentId)
        return this
    }

    fun delete(cType: ComponentType<*>, index: Int): FFContext {
        mapper<Component>(cType).delete(index)
        return this
    }

    fun deleteQuietly(cType: ComponentType<*>, index: Int): FFContext {
        val mapper = mapper<Component>(cType)
        if (mapper.map.contains(index))
            mapper.delete(index)
        return this
    }

    fun deleteAll(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            delete(cType, i)
            i = set.nextSetBit(i + 1)
        }
        return this
    }

    fun deleteAllQuietly(cType: ComponentType<*>, set: BitSet): FFContext {
        var i = set.nextSetBit(0)
        while (i >= 0) {
            deleteQuietly(cType, i)
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

    fun deleteAllQuietly(list: DynArray<CompId>): FFContext {
        val it = list.iterator()
        while (it.hasNext())
            deleteQuietly(it.next())
        return this
    }

    fun delete(cType: ComponentType<*>, indexed: Indexed): FFContext =
        delete(cType, indexed.index)

    fun deleteQuietly(cType: ComponentType<*>, indexed: Indexed): FFContext =
        deleteQuietly(cType, indexed.index)

    fun delete(id: CompId): FFContext =
        delete(id.componentType, id.instanceId)

    fun deleteQuietly(id: CompId): FFContext {
        if (id != NO_COMP_ID)
            deleteQuietly(id.componentType, id.instanceId)
        return this
    }

    fun delete(id: CompNameId): FFContext =
        delete(id.componentType, id.name)

    fun deleteQuietly(id: CompNameId): FFContext =
        deleteQuietly(id.componentType, id.name)

    fun delete(cType: ComponentType<*>, name: String): FFContext {
        mapper<Component>(cType).delete(name)
        return this
    }

    fun deleteQuietly(cType: ComponentType<*>, name: String): FFContext {
        if (name == NO_NAME)
            return this
        val mapper = mapper<Component>(cType)
        if (mapper.contains(name))
            mapper.delete(name)
        return this
    }

    fun delete(cType: ComponentType<*>, named: Named): FFContext =
        delete(cType, named.name)

    fun deleteQuietly(cType: ComponentType<*>, named: Named): FFContext =
        deleteQuietly(cType, named.name)

    fun delete(singleton: SingletonComponent<*, *>): FFContext {
        singleton.dispose()
        return this
    }


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

}