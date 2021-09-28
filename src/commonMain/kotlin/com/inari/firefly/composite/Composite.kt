package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType


abstract class Composite protected constructor() : SystemComponent(Composite::class.simpleName!!) {

    var loaded: Boolean = false
        private set
    var active: Boolean = false
        private set

    internal fun systemLoad() {
        if (!loaded) {
            loadComposite()
            loaded = true
        }
    }
    internal fun systemActivate() {
        if (!active) {
            if (!loaded)
                FFContext.load(this)

            activateComposite()
            active = true
        }
    }
    internal fun systemDeactivate()  {
        if (active) {
            deactivateComposite()
            active = false
        }
    }
    internal fun systemDispose() {
        if (loaded) {
            if (active)
                FFContext.deactivate(this)

            dispose()
            loaded = false
        }
    }

    protected abstract fun loadComposite()
    protected abstract fun activateComposite()
    protected abstract fun deactivateComposite()
    protected abstract fun disposeComposite()

    companion object : SystemComponentType<Composite>(Composite::class)
}