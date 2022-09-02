package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.collection.DynArray
import com.inari.util.indexed.Indexed

enum class RelationPolicy {
    NO_RELATION,
    CHILD,
    PARENT
}

class CLooseReference internal constructor(
    val targetType: ComponentType<*>
) {

    constructor(
        targetType: ComponentSubTypeSystem<*,*>
    ) : this(targetType.system)

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceIndex >= 0 }

    operator fun invoke(name: String) { targetKey = ComponentSystem[targetType].getKey(name, true) }
    operator fun invoke(named: Named) { targetKey = ComponentSystem[targetType].getKey(named.name, true) }
    operator fun invoke(key: ComponentKey) {
        if (key.name == NO_NAME)
            throw IllegalArgumentException("Key needs a name to be used as loose reference")
        targetKey = key
    }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
    }
}

class CLooseReferences internal constructor(
    val targetType: ComponentType<*>
) {

    constructor(
        targetType: ComponentSubTypeSystem<*,*>
    ) : this(targetType.system)

    lateinit var refKeys: DynArray<ComponentKey>
        internal set

    private var initialized = false
    private fun init() {
        if (initialized) return
        refKeys = DynArray.of(1,2)
        initialized = true
    }

    fun withReference(name: String) {
        init()
        refKeys + ComponentSystem[targetType].getKey(name, true)
    }
    fun withReference(named: Named) {
        init()
        refKeys + ComponentSystem[targetType].getKey(named.name, true)
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
}

class CReference internal constructor(
    val targetType: ComponentType<*>
) {

    constructor(
        targetType: ComponentSubTypeSystem<*,*>
    ) : this(targetType.system)

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceIndex >= 0 }

    operator fun invoke(id: ComponentId) { targetKey = ComponentSystem[targetType].getKey(id.instanceIndex) }
    operator fun invoke(index: Int) { targetKey = ComponentSystem[targetType].getKey(index) }
    operator fun invoke(indexed: Indexed) { targetKey = ComponentSystem[targetType].getKey(indexed.index) }
    operator fun invoke(name: String) { targetKey = ComponentSystem[targetType].getKey(name) }
    operator fun invoke(named: Named) { targetKey = ComponentSystem[targetType].getKey(named.name) }
    operator fun invoke(key: ComponentKey) { targetKey = (key) }
    operator fun invoke(component: Component) { targetKey = ComponentSystem[targetType].getKey(component.name) }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
    }
}