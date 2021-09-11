package com.inari.firefly.composite

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier


abstract class Composite protected constructor() : SystemComponent(Composite::class.simpleName!!) {

    var loaded: Boolean = false
        private set

    internal fun systemLoad() {
        if (!loaded) {
            load()
            loaded = true

        }
    }
    internal fun systemActivate() = activate()
    internal fun systemDeactivate() = deactivate()
    internal fun systemUnload() {
        if (loaded) {
            unload()
            loaded = false
        }
    }

    protected abstract fun load()
    protected abstract fun activate()
    protected abstract fun deactivate()
    protected abstract fun unload()

    override fun componentType() = Companion
    companion object : SystemComponentType<Composite>(Composite::class)

}