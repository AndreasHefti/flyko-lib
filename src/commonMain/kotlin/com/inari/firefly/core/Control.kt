package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.INFINITE_SCHEDULER
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.FFTimer
import com.inari.util.*
import kotlin.jvm.JvmField

abstract class Control protected constructor(): Component(Control) {

    private val updateListener = ::update

    override fun activate() =
        Engine.registerListener(UPDATE_EVENT_TYPE, updateListener)
    override fun deactivate() =
        Engine.disposeListener(UPDATE_EVENT_TYPE, updateListener)

    abstract fun update()

    override val componentType = Companion
    companion object : ComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)
        override fun create(): Control =
            throw UnsupportedOperationException("Control is abstract use a concrete implementation instead")
    }
}

interface ControlledComponent<C : Component>{

    val controllerReference: CReference

    fun withControl(name: String) = controllerReference(name)
    fun withControl(index: Int) = controllerReference(index)
    fun withControl(key: ComponentKey) = controllerReference(key)
    fun <CTRL : ComponentControl<C>> withControl(builder: ComponentBuilder<CTRL>, configure: (CTRL.() -> Unit)) {
        val control = builder.buildAndGetActive(configure)
        controllerReference(control)
    }
}

abstract class ComponentControl<C : Component> : Control() {

    private val componentListener: ComponentEventListener = { index, type ->
        val component = ComponentSystem[controlledComponentType][index]
        if (type == ComponentEventType.ACTIVATED)
            notifyActivation(component)
        else if (type == ComponentEventType.DEACTIVATED)
            notifyDeactivation(component)
    }

    abstract val controlledComponentType: ComponentType<C>
    abstract fun notifyActivation(component: C)
    abstract fun notifyDeactivation(component: C)

    override fun load() {
        super.load()
        ComponentSystem[controlledComponentType].registerComponentListener(componentListener)
    }

    override fun dispose() {
        super.dispose()
        ComponentSystem[controlledComponentType].disposeComponentListener(componentListener)
    }
}

class Scene : Control() {

    @JvmField internal var scheduler: FFTimer.Scheduler = INFINITE_SCHEDULER
    @JvmField internal var updateOperation: Operation = RUNNING_OPERATION
    @JvmField internal var callback: OperationCallback = VOID_CONSUMER
    @JvmField var deleteAfterRun: Boolean = false

    fun withCallback(callback: OperationCallback) {
        this.callback = callback
    }

    fun withUpdate(update: Operation) {
        updateOperation = update
    }

    override fun update() {
        if (!scheduler.needsUpdate())
            return

        val result = updateOperation()
        if (result == OperationResult.RUNNING)
            return

        stop(index)
        callback(result)
        if (deleteAfterRun)
            Scene.delete(index)
    }

    companion object :  ComponentSubTypeSystem<Control, Scene>(Control, "Scene") {
        override fun create() = Scene()

        fun run(index: Int, callback: OperationCallback) {
            val scene = this[index]
            scene.withCallback(callback)
            activate(index)
        }

        fun pause(index: Int) = deactivate(index)
        fun resume(index: Int) = activate(index)
        fun stop(index: Int) = dispose(index)
    }

}

