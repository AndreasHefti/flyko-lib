package com.inari.firefly.entity

import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.Component
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.entity.EntityComponent.Companion.ENTITY_COMPONENT_ASPECTS
import com.inari.util.Receiver
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.AspectType
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.indexed.AbstractIndexed
import kotlin.reflect.KClass

abstract class EntityComponent protected constructor(
    objectIndexerName: String
) : AbstractIndexed(objectIndexerName), Component {
    
    /** An EntityComponent instance has no object index (-1) only a subType index
     *  supported by the index subType key
     */
    final override val componentId: CompId
        by lazy { CompId(-1, componentType()) }

    final override fun dispose() =
        EntityProvider.dispose(this)

    var initialized: Boolean = false
        protected set

    protected var entityId: CompId = NO_COMP_ID

    internal fun internalInit(entityId: CompId) {
        this.entityId = entityId
        init()
        initialized = true
    }

    protected open fun init() {
        initialized = true
    }

    internal fun internalReset() {
        reset()
        this.entityId = NO_COMP_ID
        initialized = false
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

    protected fun <T> setIfNotInitialized(value:T, name: String): T =
        if (initialized) alreadyInit(name)
        else value

    private fun <T> alreadyInit(name: String): T =
        throw IllegalStateException("No set on already initialized property allowed for: $name")

    abstract fun reset()

    abstract fun componentType(): ComponentType<out EntityComponent>

    companion object {
        val ENTITY_COMPONENT_ASPECTS = IndexedAspectType("ENTITY_COMPONENT_ASPECTS")
    }
}

abstract class EntityComponentBuilder<C : EntityComponent> : ComponentType<C> {
    private fun doBuild(comp: C, configure: C.() -> Unit, entityId: CompId, receiver: (C) -> C): CompId {
        comp.also(configure)
        receiver(comp)
        comp.internalInit(entityId)
        return comp.componentId
    }
    internal fun builder(entityId: CompId, receiver: Receiver<C>): (C.() -> Unit) -> CompId = {
        configure -> doBuild(EntityProvider.getComponent(this), configure, entityId, receiver)
    }
    internal fun create(): C = createEmpty()
    protected abstract fun createEmpty(): C
}

abstract class EntityComponentType<C : EntityComponent>(
    final override val typeClass: KClass<out C>
) : EntityComponentBuilder<C>(), ComponentType<C> {
    val compAspect: Aspect = ENTITY_COMPONENT_ASPECTS.createAspect(typeClass.simpleName!!)
    final override val aspectIndex: Int = compAspect.aspectIndex
    final override val aspectName: String = compAspect.aspectName
    final override val aspectType: AspectType = compAspect.aspectType
}