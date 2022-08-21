package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.util.Named
import com.inari.util.indexed.Indexed

enum class RelationPolicy {
    NO_RELATION,
    CHILD,
    PARENT
}

class CReference internal constructor(
    val targetType: ComponentType<*>,
    val relationPolicy: RelationPolicy = RelationPolicy.NO_RELATION
) {

    constructor(
        targetType: ComponentSubTypeSystem<*,*>,
        relationPolicy: RelationPolicy = RelationPolicy.NO_RELATION
    ) : this(targetType.system, relationPolicy)

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.instanceId >= 0 }

    operator fun invoke(id: ComponentId) { targetKey = ComponentSystem[targetType].getKey(id.instanceId) }
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