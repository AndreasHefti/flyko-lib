package com.inari.firefly.core

import com.inari.firefly.core.api.Condition
import com.inari.firefly.core.api.TRUE_CONDITION
import kotlin.jvm.JvmField


abstract class ConditionalComponent protected constructor(): Component(ConditionalComponent) {

    abstract operator fun invoke(key1: ComponentKey = NO_COMPONENT_KEY, key2: ComponentKey = NO_COMPONENT_KEY): Boolean

    companion object : AbstractComponentSystem<ConditionalComponent>("ConditionalComponent") {
        override fun allocateArray(size: Int): Array<ConditionalComponent?> = arrayOfNulls(size)
    }
}

class Conditional private constructor(): ConditionalComponent() {

    @JvmField var condition: Condition = TRUE_CONDITION

    override fun invoke(key1: ComponentKey, key2: ComponentKey): Boolean = condition(key1, key2)

    companion object : ComponentSubTypeBuilder<ConditionalComponent, Conditional>(ConditionalComponent, "Conditional") {
        override fun create() = Conditional()
    }
}

class AndCondition private constructor(): ConditionalComponent() {

    @JvmField val left = CReference(ConditionalComponent)
    @JvmField val right = CReference(ConditionalComponent)

    override fun invoke(key1: ComponentKey, key2: ComponentKey): Boolean =
        left.exists && right.exists && ConditionalComponent[left](key1, key2) && ConditionalComponent[right](key1, key2)

    companion object : ComponentSubTypeBuilder<ConditionalComponent, AndCondition>(ConditionalComponent, "AndCondition") {
        override fun create() = AndCondition()
    }
}

class OrCondition private constructor(): ConditionalComponent() {

    @JvmField val left = CReference(ConditionalComponent)
    @JvmField val right = CReference(ConditionalComponent)

    override fun invoke(key1: ComponentKey, key2: ComponentKey): Boolean =
        (left.exists && ConditionalComponent[left](key1, key2)) || (right.exists && ConditionalComponent[right](key1, key2))

    companion object : ComponentSubTypeBuilder<ConditionalComponent, OrCondition>(ConditionalComponent, "OrCondition") {
        override fun create() = OrCondition()
    }
}