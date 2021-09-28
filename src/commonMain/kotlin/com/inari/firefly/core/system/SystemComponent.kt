package com.inari.firefly.core.system

import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.component.NamedComponent
import com.inari.firefly.core.system.SystemComponent.Companion.SYSTEM_COMPONENT_ASPECTS
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.reflect.KClass

abstract class SystemComponent protected constructor(
    objectIndexerName: String
) : AbstractIndexed(objectIndexerName), NamedComponent {

    override var name: String = NO_NAME
        set(value) {
            if (name !== NO_NAME) {
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            }
            field = value
        }

    final override val componentId: CompId
        by lazy { CompId(index, componentType()) }

    var initialized: Boolean = false
        internal set

    internal fun internalInit() {
        super.applyNewIndex()
        init()
        initialized = true
    }

    protected open fun init() {
        initialized = true
    }

    override fun dispose() {
        initialized = false
        super.disposeIndex()
    }

    protected fun setIfNotInitialized(value: Int, name: String): Int =
        if (initialized) alreadyInit(name)
        else value

    protected fun setIfNotInitialized(value: Float, name: String): Float =
        if (initialized) alreadyInit(name)
        else value

    protected fun setIfNotInitialized(value: Boolean, name: String): Boolean =
        if (initialized) alreadyInit(name)
        else value

    protected fun <T> setIfNotInitialized(value: T, name: String): T =
        if (initialized) alreadyInit(name)
        else value

    private fun <T> alreadyInit(name: String): T =
        throw IllegalStateException("Invalid modification on an immutable property of an already initialized component for: $name")

    abstract fun componentType(): ComponentType<out SystemComponent>

    companion object {
        val SYSTEM_COMPONENT_ASPECTS = IndexedAspectType("SYSTEM_COMPONENT_ASPECTS")
    }

}

abstract class SystemComponentType<C : SystemComponent>(
    final override val typeClass: KClass<C>
) : ComponentType<C> {
    val compAspect: Aspect = SYSTEM_COMPONENT_ASPECTS.createAspect(typeClass.simpleName!!)
    final override val aspectIndex: Int = compAspect.aspectIndex
    final override val aspectName: String = compAspect.aspectName
    final override val aspectType: AspectType = compAspect.aspectType
    override val subTypeClass: KClass<out C> = typeClass
    override fun toString() = "SystemComponentType:$aspectName"
}

abstract class SystemComponentSingleType<C : SystemComponent>(
    final override val typeClass: KClass<C>
) : SystemComponentBuilder<C>(), ComponentType<C> {
    final override val subTypeClass: KClass<out C>
        get() = typeClass
    final override val compAspect: Aspect = SYSTEM_COMPONENT_ASPECTS.createAspect(typeClass.simpleName!!)
    final override val aspectIndex: Int = compAspect.aspectIndex
    final override val aspectName: String = compAspect.aspectName
    final override val aspectType: AspectType = compAspect.aspectType
    override fun toString() = "SystemComponentType:$aspectName"
}

abstract class SystemComponentSubType<C : SystemComponent, CC : C>(
    baseType: SystemComponentType<C>,
    override val subTypeClass: KClass<CC>
) : SystemComponentBuilder<CC>(), ComponentType<C> {
    final override val typeClass: KClass<C> = baseType.typeClass
    final override val compAspect: Aspect = baseType.compAspect
    final override val aspectIndex: Int = baseType.aspectIndex
    final override val aspectName: String = baseType.aspectName
    final override val aspectType: AspectType = baseType.aspectType
    override fun toString() = "SystemComponentType:${aspectName}:${subTypeClass.simpleName}"
}
