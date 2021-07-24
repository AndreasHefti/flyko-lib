package com.inari.firefly.asset

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.IndexedInstantiableList
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import kotlin.jvm.JvmField

abstract class Asset protected constructor() : SystemComponent(Asset::class.simpleName!!), IndexedInstantiableList {

    @JvmField protected var dependingRef: Int = -1
    fun dependingIndex(): Int = dependingRef
    fun dependsOn(index: Int): Boolean = dependingRef == index

    override val instanceId: Int get() = instanceId(0)

    internal fun activate() = load()
    internal fun deactivate() = unload()

    protected abstract fun load()
    protected abstract fun unload()

    fun loaded():Boolean =
        FFContext.isActive(componentId)

    override fun componentType() = Companion
    companion object : SystemComponentType<Asset>(Asset::class)
}