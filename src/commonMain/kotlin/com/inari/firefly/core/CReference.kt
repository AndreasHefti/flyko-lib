package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.DynArray
import com.inari.util.indexed.Indexed

class CLooseReference internal constructor(
    val targetType: ComponentType<*>
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
    }
    operator fun invoke(key: ComponentKey) {
        targetKey = key
    }
    fun setKey(key: ComponentKey): ComponentKey {
        this(key)
        return key
    }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
    }
}

class CLooseReferences internal constructor(
    val targetType: ComponentType<*>
) {

    lateinit var refKeys: DynArray<ComponentKey>
        internal set

    private var initialized = false
    private fun init() {
        if (initialized) return
        refKeys = DynArray.of(1,2)
        initialized = true
    }

    fun withReference(named: Named) = withReference(named.name)
    fun withReference(name: String) {
        init()
        if (name === NO_NAME) return
        refKeys + ComponentSystem[targetType].getOrCreateKey(name)
    }

    fun withReference(key: ComponentKey) {
        if (key.name == NO_NAME)
            throw IllegalArgumentException("Key needs a name to be used as loose reference")
        init()
        refKeys + key
    }

    fun removeReference(name: String) {
        refKeys.forEach { key ->
            if (key.name == name)
                refKeys.remove(key)
            refKeys.trim()
        }
    }

    fun reset() {
        refKeys.clear()
        initialized = false
    }

    fun contains(index: Int): Boolean =
        refKeys.find { it.instanceIndex == index }?.let { true } ?: false
}

class CReference internal constructor(
    val targetType: ComponentType<*>
) {

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceIndex >= 0 }

    operator fun invoke(id: ComponentId) = invoke(id.instanceIndex)
    operator fun invoke(index: Int) { targetKey = ComponentSystem[targetType].getKey(index) }
    operator fun invoke(indexed: Indexed) = invoke(indexed.index)
    operator fun invoke(named: Named) = invoke(named.name)
    operator fun invoke(name: String) {
        targetKey = if (name === NO_NAME)
            NO_COMPONENT_KEY
        else
            ComponentSystem[targetType].getKey(name)
    }
    operator fun invoke(key: ComponentKey) { targetKey = (key) }
    operator fun invoke(otherRef: CReference) { targetKey = otherRef.targetKey }
    operator fun invoke(component: Component) { targetKey = ComponentSystem[targetType].getKey(component.name) }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
    }
}