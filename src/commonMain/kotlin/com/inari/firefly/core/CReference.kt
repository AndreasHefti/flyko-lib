package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.VOID_CONSUMER
import com.inari.util.collection.DynArray
import com.inari.util.indexed.Indexed

class CLooseReference internal constructor(
    val targetType: ComponentType<*>,
    val init: (ComponentKey) -> Unit = VOID_CONSUMER
) {

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceIndex >= 0 }

    operator fun invoke(named: Named) = invoke(named.name)
    operator fun invoke(name: String) {
        targetKey = if (name === NO_NAME)
            NO_COMPONENT_KEY
        else
            ComponentSystem[targetType].getOrCreateKey(name)
        init(targetKey)
    }
    operator fun invoke(key: ComponentKey) {
        targetKey = key
        init(targetKey)
    }
    fun setKey(key: ComponentKey): ComponentKey {
        this(key)
        return key
    }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
        init(targetKey)
    }
}

class CReference internal constructor(
    val targetType: ComponentType<*>,
    val init: (ComponentKey) -> Unit = VOID_CONSUMER
) {

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceIndex >= 0 }

    operator fun invoke(id: ComponentId) = invoke(id.instanceIndex)
    operator fun invoke(index: Int) {
        targetKey = ComponentSystem[targetType].getKey(index)
        init(targetKey)
    }
    operator fun invoke(indexed: Indexed) = invoke(indexed.index)
    operator fun invoke(named: Named) = invoke(named.name)
    operator fun invoke(name: String) {
        targetKey = if (name === NO_NAME)
            NO_COMPONENT_KEY
        else
            ComponentSystem[targetType].getKey(name)
        init(targetKey)
    }
    operator fun invoke(key: ComponentKey) {
        targetKey = key
        init(targetKey)
    }
    operator fun invoke(otherRef: CReference) {
        targetKey = otherRef.targetKey
        init(targetKey)
    }
    operator fun invoke(component: Component) {
        targetKey = ComponentSystem[targetType].getKey(component.name)
        init(targetKey)
    }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
        init(targetKey)
    }
}