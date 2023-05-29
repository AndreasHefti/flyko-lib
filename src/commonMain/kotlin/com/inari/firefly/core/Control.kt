package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.EntityIndex
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.core.api.SimpleTask
import com.inari.util.VOID_CALL
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator
import kotlin.jvm.JvmField

object Pausing {

    var paused = false
        private set

    private val pausedGroups: Aspects = Component.COMPONENT_GROUP_ASPECT.createAspects()

    fun pause(group: Aspect) {
        pausedGroups + group
        paused = !pausedGroups.isEmpty
    }

    fun pauseExclusive(group: Aspect) {
        pausedGroups.clear()
        pausedGroups + group
        paused = !pausedGroups.isEmpty
    }

    fun pauseAll(groups: Aspects) {
        pausedGroups + groups
        paused = !pausedGroups.isEmpty
    }

    fun pauseAllExclusive(groups: Aspects) {
        pausedGroups.clear()
        pausedGroups + groups
        paused = !pausedGroups.isEmpty
    }

    fun resume(group: Aspect) {
        pausedGroups - group
        paused = !pausedGroups.isEmpty
    }

    fun resumeAll(groups: Aspects) {
        pausedGroups - groups
        paused = !pausedGroups.isEmpty
    }

    fun resumeAll() {
        pausedGroups.clear()
        paused = false
    }

    fun isPaused(group: Aspect): Boolean = paused && group in pausedGroups
    fun isPaused(groups: Aspects?): Boolean = paused && (groups != null && pausedGroups.intersects(groups))
}

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

    companion object : AbstractComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)

        init {
           Engine.registerListener(UPDATE_EVENT_TYPE, ::updateAllActiveControls)
        }

        private fun updateAllActiveControls() {
            val iter = Control.activeIndexIterator()
            if (Pausing.paused)
                while (iter.hasNext()) {
                    val c = Control[iter.nextInt()]
                    if (c.scheduler.needsUpdate() && !Pausing.isPaused(c.groups))
                        c.update()
                }
            else
                while (iter.hasNext()) {
                    val c = Control[iter.nextInt()]
                    if (c.scheduler.needsUpdate())
                        c.update()
                }
        }
    }
}

class UpdateControl private constructor() : Control() {

    @JvmField var updateOp: SimpleTask = VOID_CALL

    override fun update() = updateOp()

    companion object : SubComponentBuilder<Control, UpdateControl>(Control) {
        override fun create() = UpdateControl()
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

    @Suppress("UNCHECKED_CAST")
    protected val system: IComponentSystem<C> = ComponentSystem[controlledType] as IComponentSystem<C>
    protected var controlledComponentKey = NO_COMPONENT_KEY

    override fun register(key: ComponentKey) {
        if (key.type.aspectIndex != controlledType.aspectIndex)
            throw IllegalArgumentException("Type mismatch")
        if (key.componentIndex < 0)
            throw IllegalArgumentException("Invalid Key -1")
        controlledComponentKey = key
    }

    override fun update() {
        if (controlledComponentKey.componentIndex > NULL_COMPONENT_INDEX && system.isActive(controlledComponentKey))
            update(system[controlledComponentKey])
        else if (controlledComponentKey.componentIndex == NULL_COMPONENT_INDEX)
            if (deleteOnControlledDelete)
                Control.delete(this)
            else
                controlledComponentKey = NO_COMPONENT_KEY
    }

    abstract fun update(component: C)

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
        val it = IndexIterator(controlledComponents)
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

abstract class EntityControl protected constructor() : Control() {

    protected val entityIndexes = BitSet()
    private val componentListener: ComponentEventListener = { key, type ->
        if (type == ComponentEventType.ACTIVATED && matchForControl(key.componentIndex)) {
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
        val it = IndexIterator(entityIndexes)
        if (Pausing.paused)
            while (it.hasNext()) {
                val entity = Entity[it.nextInt()]
                if (!Pausing.isPaused(entity.groups))
                    update(entity.index)
            }
        else
            while (it.hasNext())
                update(it.nextInt())
    }

    abstract fun matchForControl(index: EntityIndex): Boolean
    protected abstract fun update(index: EntityIndex)
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


