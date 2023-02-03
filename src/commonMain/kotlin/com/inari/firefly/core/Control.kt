package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class Control protected constructor() : Component(Control) {

    init {
        autoLoad = true
        autoActivation = true
    }

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    var updateResolution: Float
        get() = scheduler.resolution
        set(value) { scheduler = Engine.timer.createUpdateScheduler(value) }

    protected abstract fun update()

    companion object : ComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)
        override fun create() = throw UnsupportedOperationException("Control is abstract use sub type builder instead")

        init {
           Engine.registerListener(UPDATE_EVENT_TYPE, ::updateAllActiveControls)
        }

        private fun updateAllActiveControls() {
            var i = Control.ACTIVE_COMPONENT_MAPPING.nextSetBit(0)
            while (i >= 0) {
                val c = Control[i]
                if (c.scheduler.needsUpdate())
                    c.update()
                i = Control.ACTIVE_COMPONENT_MAPPING.nextSetBit(i + 1)
            }
//            val iter = Control.activeIterator()
//            while (iter.hasNext()) {
//                val it = iter.next()
//                if (it.scheduler.needsUpdate())
//                    it.update()
//            }
        }
    }
}

abstract class ComponentControl protected constructor() : Control() {
    abstract val controlledType : ComponentType<*>
    abstract fun register(key: ComponentKey)
}

abstract class SingleComponentControl<C : Component> protected constructor(
    final override val controlledType: ComponentType<*>
) : ComponentControl() {

    @JvmField val deleteOnControlledDelete = true

    protected var controlledComponentKey = NO_COMPONENT_KEY

    override fun register(key: ComponentKey) {
        if (key.type.aspectIndex != controlledType.aspectIndex)
            throw IllegalArgumentException("Type mismatch")
        if (key.componentIndex < 0)
            throw IllegalArgumentException("Invalid Key -1")
        controlledComponentKey = key
        init(controlledComponentKey)
    }

    override fun update() {
        if (controlledComponentKey.componentIndex < 0) return
        val c: C = ComponentSystem[controlledComponentKey]
        if (c.active)
            update(c)
        else if (c.index < 0)
            if (deleteOnControlledDelete)
                Control.delete(this)
            else
                controlledComponentKey = NO_COMPONENT_KEY
    }

    abstract fun init(key: ComponentKey)
    abstract fun update(c: C)
}


abstract class ComponentsControl<C : Component> protected constructor(
    final override val controlledType: ComponentType<*>
) : ComponentControl() {

    @Suppress("UNCHECKED_CAST")
    protected val system: IComponentSystem<C> = ComponentSystem[controlledType] as IComponentSystem<C>
    protected var controlledComponents = BitSet()

    override fun register(key: ComponentKey) {
        if (key.type.aspectIndex != controlledType.aspectIndex)
            throw IllegalArgumentException("Type mismatch")
        if (key.componentIndex < 0)
            throw IllegalArgumentException("Invalid Key -1")
        controlledComponents.set(key.componentIndex)
    }

    override fun update() {
        val it = controlledComponents.iterator()
        while (it.hasNext()) {
            val componentIndex = it.nextInt()
            if (!system.exists(componentIndex)) {
                controlledComponents[componentIndex] = false
                continue
            }

            val c = system[componentIndex]
            if (c.active)
                update(c)
        }
    }

    abstract fun update(c: C)

}

abstract class EntityControl  protected constructor() : Control() {

    protected val entityIndexes = BitSet()
    private val componentListener: ComponentEventListener = { key, type ->
        if (type == ComponentEventType.ACTIVATED && matchForControl(Entity[key])) {
            entityIndexes[key.componentIndex] = true
            if (!this.active)
                Control.activate(this)
        }
        else if (type == ComponentEventType.DEACTIVATED && key.componentIndex < entityIndexes.size)
            entityIndexes[key.componentIndex] = false
    }

    override fun load() {
        super.load()
        Entity.registerComponentListener(componentListener)
    }

    override fun dispose() {
        super.dispose()
        Entity.disposeComponentListener(componentListener)
    }

    override fun update() {
        var index = entityIndexes.nextSetBit(0)
        while (index >= 0) {
            update(index)
            index = entityIndexes.nextSetBit(index + 1)
        }
//        val it = entityIndexes.iterator()
//        while (it.hasNext())
//            update(it.nextInt())
    }

    abstract fun matchForControl(entity: Entity): Boolean
    protected abstract fun update(entityId: Int)
}

interface Controlled {

    fun earlyKeyAccess(): ComponentKey

    fun withControl(name: String) = (Control[name] as ComponentControl).register(earlyKeyAccess())
    fun withControl(key: ComponentKey) = (Control[key] as ComponentControl).register(earlyKeyAccess())
    fun <CTRL : ComponentControl> withControl(builder: ComponentBuilder<CTRL>, configure: (CTRL.() -> Unit)) {
        val control = builder.buildAndGet(configure)
        control.register(earlyKeyAccess())
    }
}


