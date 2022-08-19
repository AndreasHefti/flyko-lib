package com.inari.firefly.core

abstract class Control protected constructor(): Component(Control) {

    private val updateListener = ::update

    override fun activate() =
        Engine.registerListener(Engine.UpdateEvent, updateListener)
    override fun deactivate() =
        Engine.disposeListener(Engine.UpdateEvent, updateListener)

    abstract fun update()

    override val componentType = Companion
    companion object : ComponentSystem<Control>("Control") {
        override fun allocateArray(size: Int): Array<Control?> = arrayOfNulls(size)
        override fun create(): Control =
            throw UnsupportedOperationException("Control is abstract use a concrete implementation instead")
    }
}

abstract class ComponentControl<C : Component> : Control() {

    private val componentListener: ComponentEventListener = { index, type ->
        val component = ComponentSystem[controlledComponentType][index]
        when (type) {
            ComponentEventType.ACTIVATED -> notifyActivation(component)
            ComponentEventType.DEACTIVATED -> notifyDeactivation(component)
            else -> {}
        }
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

//abstract class SingleComponentControl : ComponentControl() {
//
//    protected var controlledIndex: CompIndex = -1
//
//    override fun register(index: Int) { controlledIndex = index }
//    override fun unregister(index: Int) { controlledIndex = -1 }
//
//}
//
//abstract class ComponentsControl : ComponentControl() {
//
//    protected val controlled: BitSet = BitSet()
//
//    override fun register(index: Int) { controlled[index] = true }
//    override fun unregister(index: Int) { controlled[index] = false }
//
//}

//class SingleComponentOp : SingleComponentControl() {
//
//    val operation = ComponentOpRef()
//    @JvmField var secondComponentRef: ComponentKey = NO_COMPONENT_KEY
//    @JvmField var thirdComponentRef: ComponentKey = NO_COMPONENT_KEY
//
//    override fun update() {
//        operation(controlledIndex, secondComponentRef.instanceId, thirdComponentRef.instanceId)
//    }
//
//    companion object : ComponentSubTypeSystem<Control, SingleComponentOp>(Control) {
//        override fun create() = SingleComponentOp()
//    }
//}

//fun test() {
//    val controlKey = SingleComponentOp.build {
//        updateResolution = 4f
//        secondComponentRef = NO_COMPONENT_KEY
//        operation("someOp")
//    }
//
//    val control: SingleComponentOp = SingleComponentOp[controlKey]
//
//    View.build {
//        name = "test"
//        shader.build {
//
//        }
//    }
//}