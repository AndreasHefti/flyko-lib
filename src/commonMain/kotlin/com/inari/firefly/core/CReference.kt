package com.inari.firefly.core

import com.inari.firefly.core.Component.Companion.NO_COMPONENT_KEY
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.NO_NAME
import com.inari.util.Named
import com.inari.util.VOID_CONSUMER_1

class CReference internal constructor(
    val targetType: ComponentType<*>,
    val init: (ComponentKey) -> Unit = VOID_CONSUMER_1
) {

    var targetKey: ComponentKey = NO_COMPONENT_KEY
        internal set

    val defined: Boolean
        get() { return targetKey != NO_COMPONENT_KEY }
    val exists: Boolean
        get() { return targetKey.componentIndex > NULL_COMPONENT_INDEX }
    val refIndex: ComponentIndex
        get() = if (exists) targetKey.componentIndex else throw IllegalStateException("Reference does not exists yet")

    operator fun invoke(index: ComponentIndex) {
        targetKey = ComponentSystem[targetType].getKey(index)
        init(targetKey)
    }
    operator fun invoke(named: Named) = invoke(named.name)
    operator fun invoke(name: String) {
        targetKey = if (name === NO_NAME)
            NO_COMPONENT_KEY
        else
            ComponentSystem[targetType].getOrCreateKey(name)
        init(targetKey)
    }
    operator fun invoke(key: ComponentKey) {
        if (key != NO_COMPONENT_KEY && key.type.aspectIndex != this.targetType.aspectIndex)
            throw IllegalArgumentException("Reference type mismatch")
        targetKey = key
        init(targetKey)
    }
    operator fun invoke(ref: CReference) {
        if (ref.targetType.aspectIndex != this.targetType.aspectIndex)
            throw IllegalArgumentException("Reference type mismatch")
        targetKey = ref.targetKey
        init(targetKey)
    }

    fun reset() {
        targetKey = NO_COMPONENT_KEY
        init(targetKey)
    }

    override fun toString(): String {
        return "${targetType.typeName} ${targetType.subTypeName} $targetKey"
    }
}