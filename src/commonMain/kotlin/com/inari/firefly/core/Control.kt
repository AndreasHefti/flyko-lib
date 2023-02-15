package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.api.SimpleTask
import com.inari.util.VOID_CONSUMER_1
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator
import com.inari.util.event.Event
import kotlin.jvm.JvmField

enum class PauseEventType {
    PAUSE,
    RESUME
}

typealias PauseEventListener = (PauseEventType, Aspects?) -> Unit
val PAUSE_EVENT_TYPE = Event.EventType("PauseEvent")
class PauseEvent internal constructor(override val eventType: EventType): Event<PauseEventListener>() {
    internal var type: PauseEventType = PauseEventType.PAUSE
    internal var groups: Aspects? = null
    override fun notify(listener: PauseEventListener) = listener(type,  groups)

    companion object {
        private val event = PauseEvent(PAUSE_EVENT_TYPE)
        fun notifyPause(groups: Aspects?) {
            event.type = PauseEventType.PAUSE
            event.groups = groups
            Engine.notify(event)
        }
        fun notifyResume() {
            event.type = PauseEventType.RESUME
            event.groups = null
            Engine.notify(event)
        }
    }
}

abstract class Control protected constructor() : Component(Control) {

    init {
        autoLoad = true
        autoActivation = true
    }

    var paused = false
        private set
    private var pausedGroups: Aspects? = null
    private val pauseEventListener: PauseEventListener = { type, groups ->
        if (type == PauseEventType.PAUSE) {
            paused = true
            pausedGroups = if (groups != null && !groups.isEmpty) groups else null
        } else {
            paused = false
            pausedGroups = null
        }
    }
    fun isPaused(group: Aspect): Boolean = paused && (pausedGroups == null || group in pausedGroups!!)
    fun isPaused(groups: Aspects?): Boolean = paused && (pausedGroups == null ||
            (groups != null && pausedGroups!!.intersects(groups)))

    override fun activate() {
        super.activate()
        Engine.registerListener(PAUSE_EVENT_TYPE, pauseEventListener)
    }

    override fun deactivate() {
        Engine.disposeListener(PAUSE_EVENT_TYPE, pauseEventListener)
        super.deactivate()
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
            while (iter.hasNext()) {
                val c = Control[iter.next()]
                if (!c.active) continue
                if (!c.isPaused(c.groups.aspects) && c.scheduler.needsUpdate())
                    c.update()
            }
        }
    }
}

class UpdateControl private constructor() : Control() {

    @JvmField var updateOp: SimpleTask = VOID_CONSUMER_1

    override fun update() = updateOp(this.index)

    companion object : ComponentSubTypeBuilder<Control, UpdateControl>(Control, "UpdateControl") {
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
        val it = IndexIterator(entityIndexes)
        while (it.hasNext())
            update(it.nextInt())
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


