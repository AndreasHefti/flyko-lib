package com.inari.firefly.core

import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE

abstract class Control protected constructor(): ComponentNode(Control) {

    private val updateListener = ::update

    override fun activate() =
        Engine.registerListener(UPDATE_EVENT_TYPE, updateListener)
    override fun deactivate() =
        Engine.disposeListener(UPDATE_EVENT_TYPE, updateListener)

    abstract fun update()

    companion object : ComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)
        override fun create(): Control =
            throw UnsupportedOperationException("Control is abstract use a concrete implementation instead")
    }
}

abstract class ComponentControl() : Control() {
    abstract fun register(name: String)
    abstract fun unregister(name: String)
}

abstract class SingleComponentControl(controlledType: ComponentType<*>) : ComponentControl() {

    constructor(subType: ComponentSubTypeSystem<*,*>) : this(subType.system)

    internal val controlledComponent = CLooseReference(controlledType)
    val controlledComponentIndex: Int
        get() = controlledComponent.targetKey.instanceIndex

    override fun register(name: String) = controlledComponent(name)
    override fun unregister(name: String) = controlledComponent.reset()

}

abstract class ComponentsControl(controlledType: ComponentType<*>) : ComponentControl() {

    constructor(subType: ComponentSubTypeSystem<*,*>) : this(subType.system)

    internal val controlledComponents = CLooseReferences(controlledType)

    override fun register(name: String) = controlledComponents.withReference(name)
    override fun unregister(name: String) = controlledComponents.removeReference(name)

    override fun activate() {
        cleanControlledReferences()
        super.activate()
    }

    private fun cleanControlledReferences() {
        controlledComponents.refKeys.forEach { key ->
            if (key.instanceIndex < 0)
                controlledComponents.refKeys.remove(key)
            controlledComponents.refKeys.trim()
        }
    }
    fun clearControlledReferences() = controlledComponents.reset()

}

interface ControlledComponent<C : Component> {

    val name: String

    fun withControl(name: String) = register(Control[name])
    fun withControl(index: Int) = register(Control[index])
    fun withControl(key: ComponentKey) = register(Control[key])
    fun <CTRL : ComponentControl> withControl(builder: ComponentBuilder<CTRL>, configure: (CTRL.() -> Unit)) {
        val control = builder.buildAndGet(configure)
        register(control)
    }

    fun withChild(key: ComponentKey): ComponentKey

    private fun register(control: Control) {
        with(control as ComponentControl) {
            register(name)
        }
        withChild(Control.getKey(control.index))
    }
}

abstract class EntityControl : Control() {

    private val componentListener: ComponentEventListener = { index, type ->
        val entity = Entity[index]
        if (type == ComponentEventType.ACTIVATED)
            notifyActivation(entity)
        else if (type == ComponentEventType.DEACTIVATED)
            notifyDeactivation(entity)
    }

    abstract fun notifyActivation(entity: Entity)
    abstract fun notifyDeactivation(entity: Entity)

    override fun load() {
        super.load()
        Entity.registerComponentListener(componentListener)
    }

    override fun dispose() {
        super.dispose()
        Entity.disposeComponentListener(componentListener)
    }
}



