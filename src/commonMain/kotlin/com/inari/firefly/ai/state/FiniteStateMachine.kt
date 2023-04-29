package com.inari.firefly.ai.state

import com.inari.firefly.core.*
import com.inari.util.FALSE_SUPPLIER
import com.inari.util.NO_NAME
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import com.inari.util.event.Event
import kotlin.jvm.JvmField

@ComponentDSL
class StateChange internal constructor() {

    var name: String = NO_NAME
        set(value) {
            if (name !== NO_NAME)
                throw IllegalStateException("An illegal reassignment of name: $value to: $name")
            field = value
        }

    @JvmField var fromState: String = NO_NAME
    @JvmField var toState: String = NO_NAME
    @JvmField var condition: () -> Boolean = FALSE_SUPPLIER

    @JvmField
    val disposeStateTaskRef = CReference(Task)
    fun withDisposeStateTask(configure: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(configure)
        disposeStateTaskRef(key)
        return key
    }

    @JvmField
    val initStateTaskRef = CReference(Task)
    fun withInitStateTask(configure: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(configure)
        initStateTaskRef(key)
        return key
    }

}

class FiniteStateMachine : Control() {

    @JvmField internal val states: DynArray<String> = DynArray.of()
    @JvmField internal val stateChanges: DynArray<StateChange> = DynArray.of()
    @JvmField internal val stateStateChanges: DynArray<StateChange> = DynArray.of()

    @JvmField var startState = NO_STATE
    @JvmField val allStates: DynArrayRO<String> = states
    @JvmField val allStateChanges: DynArrayRO<StateChange> = stateChanges
    @JvmField val currentStateChanges: DynArrayRO<StateChange> = stateStateChanges

    var currentState: String = NO_STATE
        internal set(value) {
            field = value
            stateStateChanges.clear()
            val iter = stateChanges.iterator()
            while (iter.hasNext()) {
                val st = iter.next()
                if (st.fromState == currentState)
                    stateStateChanges.add(st)
            }
        }

    fun withState(state: String) = states + state
    fun withStates(vararg states: String)  {
        val iter = states.iterator()
        while (iter.hasNext())
            this.states + iter.next()
    }

    fun withStateChange(configure: (StateChange.() -> Unit)) {
        val stateChange = StateChange()
        configure(stateChange)
        stateChanges + stateChange
        if (stateChange.fromState == currentState)
            stateStateChanges + stateChange
    }

    fun findStateChangeForTargetState(targetStateName: String): StateChange? {
        val it = stateChanges.iterator()
        while (it.hasNext()) {
            val sc = it.next()
            if (sc.fromState == currentState && targetStateName == sc.toState)
                return sc
        }
        return null
    }

    fun findStateChangeForCurrentState(stateChangeName: String): StateChange? {
        val it = stateChanges.iterator()
        while (it.hasNext()) {
            val sc = it.next()
            if (stateChangeName == sc.name && sc.fromState == currentState)
                return sc
        }
        return null
    }

    override fun activate() {
        currentState = startState
        sendEvent(
            StateChangeEvent.Type.STATE_MACHINE_STARTED,
            Control.getKey(this.index)
        )
    }

    override fun deactivate() {
        this.reset()
        sendEvent(
            StateChangeEvent.Type.STATE_MACHINE_FINISHED,
            Control.getKey(this.index)
        )
    }

    fun reset() {
        currentState = startState
    }

    override fun update() {
        var i = 0
        while (i < currentStateChanges.capacity) {
            val stateChange = currentStateChanges[i++] ?: continue
            if (stateChange.condition()) {
                if (stateChange.disposeStateTaskRef.exists)
                    Task[stateChange.disposeStateTaskRef.targetKey](this.key)

                currentState = stateChange.toState

                if (stateChange.initStateTaskRef.exists)
                    Task[stateChange.initStateTaskRef.targetKey](this.key)

                if (stateChange.toState !== NO_STATE)
                    sendEvent(
                        StateChangeEvent.Type.STATE_CHANGED,
                        Control.getKey(this.index),
                        stateChange
                    )
                else
                    sendEvent(
                        StateChangeEvent.Type.STATE_MACHINE_FINISHED,
                        Control.getKey(this.index),
                        stateChange
                    )
            }
        }
    }

    companion object : ComponentSubTypeBuilder<Control, FiniteStateMachine>(Control, "FiniteStateMachine") {
        const val NO_STATE: String = "[[NO_STATE]]"
        val STATE_CHANGE_EVENT_TYPE = Event.EventType("StateChangeEvent")
        override fun create() = FiniteStateMachine()

        private val workflowEvent = StateChangeEvent(STATE_CHANGE_EVENT_TYPE)
        internal fun sendEvent(
            type: StateChangeEvent.Type,
            machineKey: ComponentKey,
            stateChangeName: String = NO_NAME,
            fromName: String = NO_NAME,
            toName: String = NO_NAME
        ) {
            workflowEvent.type = type
            workflowEvent.machineKey = machineKey
            workflowEvent.stateChangeName = stateChangeName
            workflowEvent.fromName = fromName
            workflowEvent.toName = toName
            Engine.notify(workflowEvent)
        }

        internal fun sendEvent(
            type: StateChangeEvent.Type,
            machineKey: ComponentKey,
            stateChange: StateChange
        ) {
            workflowEvent.type = type
            workflowEvent.machineKey = machineKey
            workflowEvent.stateChangeName = stateChange.name
            workflowEvent.fromName = stateChange.fromState
            workflowEvent.toName = stateChange.toState
            Engine.notify(workflowEvent)
        }
    }
}

class StateChangeEvent(override val eventType: EventType) : Event<(StateChangeEvent) -> Unit>() {

    enum class Type {
        STATE_MACHINE_STARTED,
        STATE_CHANGED,
        STATE_MACHINE_FINISHED
    }

    lateinit var type: Type
        internal set
    var machineKey: ComponentKey = Component.NO_COMPONENT_KEY
        internal set
    var stateChangeName = NO_NAME
        internal set
    var fromName = NO_NAME
        internal set
    var toName = NO_NAME
        internal set

    override fun notify(listener: (StateChangeEvent) -> Unit) = listener(this)
}

class StateMachineEventTrigger  private constructor() : Trigger() {

    private val stateChangeEventListener = { doTrigger() }

    override fun load() = Engine.registerListener(FiniteStateMachine.STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)
    override fun dispose() = Engine.disposeListener(FiniteStateMachine.STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)

    companion object : SubComponentBuilder<Trigger, StateMachineEventTrigger>(Trigger) {
        override fun create() = StateMachineEventTrigger()
    }
}