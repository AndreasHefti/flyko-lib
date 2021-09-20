package com.inari.firefly.composite

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType


abstract class Composite protected constructor() : SystemComponent(Composite::class.simpleName!!) {

    var loaded: Boolean = false
        private set
    var active: Boolean = false
        private set

    internal fun systemLoad() {
        if (!loaded) {
            load()
            loaded = true
        }
    }
    internal fun systemActivate() {
        if (!active) {
            if (!loaded)
                load()

            activate()
            active = true
        }
    }
    internal fun systemDeactivate()  {
        if (active) {
            deactivate()
            active = false
        }
    }
    internal fun systemUnload() {
        if (loaded) {
            if (active)
                systemDeactivate()

            unload()
            loaded = false
        }
    }

    protected abstract fun load()
    protected abstract fun activate()
    protected abstract fun deactivate()
    protected abstract fun unload()

    companion object : SystemComponentType<Composite>(Composite::class)
}