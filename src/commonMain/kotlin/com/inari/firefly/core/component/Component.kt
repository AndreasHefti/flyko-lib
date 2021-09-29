package com.inari.firefly.core.component

import com.inari.firefly.core.ComponentRefPredicate
import com.inari.firefly.core.ComponentRefResolver
import com.inari.util.Named
import com.inari.util.aspect.Aspect
import com.inari.util.indexed.Indexed
import kotlin.reflect.KClass

@DslMarker
annotation class ComponentDSL

@ComponentDSL
interface Component : Indexed {
    val componentId: CompId
    fun dispose()
}

interface NamedComponent : Component, Named

interface ComponentType<C : Component> : Aspect {
    val typeClass: KClass<C>
    val subTypeClass: KClass<out C>
}

class CompId (
    val instanceId: Int,
    val componentType: ComponentType<*>
) : Indexed {

    fun checkType(typeAspect: Aspect): CompId {
        componentType.aspectType.typeCheck(typeAspect)
        return this
    }

    override val index: Int = instanceId
    override val indexedTypeName: String
        get() = componentType.aspectName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CompId

        if (instanceId != other.instanceId) return false
        if (componentType != other.componentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instanceId
        result = 31 * result + componentType.hashCode()
        return result
    }

    override fun toString(): String {
        return "CompId(instanceId=$instanceId, componentType=$componentType)"
    }
}

interface CompNameId: Named {
    val componentType: ComponentType<*>
}

abstract class ComponentBuilder<out C : Component> {
    protected abstract fun createEmpty(): C
}

data class ComponentLoadDispatcher<C : Component>(
    val loadDispatch: ComponentRefResolver<C>,
    val isLoaded: ComponentRefPredicate<C>,
    val disposeDispatch: ComponentRefResolver<C>
)