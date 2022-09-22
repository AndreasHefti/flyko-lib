package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.FFTimer
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class Control protected constructor() : Component(Control) {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }

    override fun activate() =
        Engine.registerListener(UPDATE_EVENT_TYPE, ::internalUpdate)
    override fun deactivate() =
        Engine.disposeListener(UPDATE_EVENT_TYPE, ::internalUpdate)

    private fun internalUpdate() {
        if (scheduler.needsUpdate())
            update()
    }

    abstract fun update()

    companion object : ComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)
        override fun create() = throw UnsupportedOperationException("Control is abstract use sub type builder instead")
    }
}

abstract class SystemControl protected constructor(
    val controlledType: ComponentType<*>
) : Control() {

    init {
        autoLoad = true
    }

    protected val componentIndexes = BitSet()
    private val componentListener: ComponentEventListener = { key, type ->
        if (type == ComponentEventType.ACTIVATED && matchForControl(key)) {
            componentIndexes[key.instanceIndex] = true
            if (!this.active)
                Control.activate(this)
        }
        else if (type == ComponentEventType.DEACTIVATED && key.instanceIndex < componentIndexes.size)
            componentIndexes[key.instanceIndex] = false
    }

    override fun load() {
        super.load()
        ComponentSystem[controlledType].registerComponentListener(componentListener)
    }

    override fun dispose() {
        super.dispose()
        ComponentSystem[controlledType].disposeComponentListener(componentListener)
    }

    override fun update() {
        var index = componentIndexes.nextSetBit(0)
        while (index >= 0) {
            update(index)
            index = componentIndexes.nextSetBit(index + 1)
        }
    }

    abstract fun matchForControl(key: ComponentKey): Boolean
    abstract fun update(index: Int)

}

class ControllerReferences(val componentType: ComponentType<out Component>) {

    private var indexes: BitSet? = null

    fun clear() = indexes?.clear()
    operator fun contains(index: Int) = indexes?.get(index) ?: false
    internal fun register(key: ComponentKey) {
        if (key === Component.NO_COMPONENT_KEY)
            throw IllegalStateException("Illegal control key, NO_COMPONENT_KEY")
        if (key.instanceIndex < 0)
            throw IllegalStateException("Control key has no instance yet")

        val control = Control[key] as SystemControl
        if (componentType.aspectIndex != control.controlledType.aspectIndex)
            throw IllegalStateException("Illegal control key, control type mismatch: ${control.controlledType.subTypeName} : ${componentType.typeName}")

        if (indexes == null)
            indexes = BitSet()
        indexes?.set(key.instanceIndex, true)
    }
    internal fun dispose(index: Int) = indexes?.set(index, false)
}

interface Controlled {

    val controllerReferences: ControllerReferences

    fun withControl(name: String) = controllerReferences.register(Control[name].key)
    fun withControl(key: ComponentKey) = controllerReferences.register(key)
    fun <CTRL : SystemControl> withControl(builder: ComponentBuilder<CTRL>, configure: (CTRL.() -> Unit)) =
        controllerReferences.register(builder.build(configure))

}



