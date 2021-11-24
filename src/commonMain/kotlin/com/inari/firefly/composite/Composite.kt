package com.inari.firefly.composite

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import kotlin.jvm.JvmField


abstract class Composite protected constructor() : SystemComponent(Composite::class.simpleName!!) {

    @JvmField var parentName = NO_NAME
    @JvmField var loadDependsOnParent = true
    @JvmField var activationDependsOnParent = false
    @JvmField var deactivateAlsoParent = false
    @JvmField var disposeAlsoParent = false

    var loaded: Boolean = false
        private set
    var active: Boolean = false
        private set

    internal fun systemLoad() {
        if (!loaded) {

            // if depends on parent and parent is defined load the parent first if not already loaded
            if (loadDependsOnParent && parentName != NO_NAME)
                FFContext.load(Composite, parentName)

            loadComposite()
            loaded = true
        }
    }
    internal fun systemActivate() {
        if (!active) {
            if (!loaded)
                FFContext.load(this)

            // if depends on parent and parent is defined activate the parent first if not already active
            if (activationDependsOnParent && parentName != NO_NAME)
                FFContext.activate(Composite, parentName)

            activateComposite()
            active = true
        }
    }
    internal fun systemDeactivate()  {
        if (active) {

            deactivateComposite()
            active = false

            // if depends on parent and parent is defined deactivate the parent also
            if (deactivateAlsoParent && parentName != NO_NAME)
                FFContext.deactivate(Composite, parentName)
        }
    }
    internal fun systemDispose() {
        if (loaded) {
            if (active)
                FFContext.deactivate(this)

            dispose()
            loaded = false

            // if depends on parent and parent is defined dispose the parent also
            if (disposeAlsoParent && parentName != NO_NAME)
                FFContext.dispose(Composite, parentName)
        }
    }

    protected abstract fun loadComposite()
    protected abstract fun activateComposite()
    protected abstract fun deactivateComposite()
    protected abstract fun disposeComposite()

    companion object : SystemComponentType<Composite>(Composite::class)
}