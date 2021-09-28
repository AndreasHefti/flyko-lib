package com.inari.firefly.core

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SingletonComponent
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.Consumer
import com.inari.util.Named
import com.inari.util.indexed.Indexed

class ComponentRefResolver<T : Component>(
    private val type: ComponentType<T>,
    private val receiver: Consumer<Int>
) {

    operator fun invoke(id: CompId) = receiver(id.instanceId)
    operator fun invoke(index: Int) = receiver(index)
    operator fun invoke(indexed: Indexed) = receiver(indexed.index)
    operator fun invoke(name: String) = receiver(FFContext[type, name].index)
    operator fun invoke(named: Named) = receiver(FFContext[type, named.name].index)
    operator fun invoke(component: Component) = receiver(component.index)
    operator fun invoke(component: SystemComponent) = receiver(component.index)
    operator fun invoke(singleton: SingletonComponent<*, *>) = receiver(singleton.instance.index)

}

class ComponentIdResolver<T : Component>(
    private val type: ComponentType<T>,
    private val receiver: Consumer<CompId>
) {

    operator fun invoke(id: CompId) = receiver(id)
    operator fun invoke(name: String) = receiver(FFContext[type, name].componentId)
    operator fun invoke(named: Named) = receiver(FFContext[type, named.name].componentId)
    operator fun invoke(component: Component) = receiver(component.componentId)
    operator fun invoke(component: SystemComponent) = receiver(component.componentId)
    operator fun invoke(singleton: SingletonComponent<*, *>) = receiver(singleton.instance.componentId)

}

class ComponentRefFunction<T : Component, X>(
    private val type: ComponentType<T>,
    private val receiver: (Int) -> X
) {

    operator fun invoke(id: CompId): X = receiver(id.instanceId)
    operator fun invoke(index: Int): X = receiver(index)
    operator fun invoke(indexed: Indexed): X = receiver(indexed.index)
    operator fun invoke(name: String): X = receiver(FFContext[type, name].index)
    operator fun invoke(named: Named): X = receiver(FFContext[type, named.name].index)
    operator fun invoke(component: Component): X = receiver(component.index)
    operator fun invoke(component: SystemComponent): X = receiver(component.index)
    operator fun invoke(singleton: SingletonComponent<*, *>): X = receiver(singleton.instance.index)

}

class ComponentRefPredicate<T : Component>(
    private val type: ComponentType<T>,
    private val predicate: (Int) -> Boolean
) {

    operator fun invoke(id: CompId): Boolean = predicate(id.instanceId)
    operator fun invoke(index: Int): Boolean = predicate(index)
    operator fun invoke(indexed: Indexed): Boolean = predicate(indexed.index)
    operator fun invoke(name: String): Boolean = predicate(FFContext[type, name].index)
    operator fun invoke(named: Named): Boolean = predicate(FFContext[type, named.name].index)
    operator fun invoke(component: Component): Boolean = predicate(component.index)
    operator fun invoke(component: SystemComponent): Boolean = predicate(component.index)
    operator fun invoke(singleton: SingletonComponent<*, *>): Boolean = predicate(singleton.instance.index)

}
