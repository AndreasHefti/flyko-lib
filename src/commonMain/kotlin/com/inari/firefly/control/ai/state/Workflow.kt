package com.inari.firefly.control.ai.state

import com.inari.firefly.NO_STATE
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO
import kotlin.jvm.JvmField


class Workflow private constructor() : SystemComponent(Workflow::class.simpleName!!) {

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
            stateChanges.forEach { st ->
                if (st.fromState == currentState)
                    stateStateChanges.add(st)
            }
        }

    fun withState(state: String) = states + state
    fun withStates(vararg states: String) = states.forEach {
        this.states + it
    }

    fun withStateChange(configure: (StateChange.() -> Unit)) {
        val stateChange = StateChange()
        stateChange.also(configure)
        stateChanges + stateChange
        if (stateChange.fromState == currentState)
            currentStateChanges + stateChange
    }

    fun findStateChangeForTargetState(targetStateName: String): StateChange? =
        stateChanges.firstOrNull {
            it.fromState == currentState && targetStateName == it.toState
        }


    fun findStateChangeForCurrentState(stateChangeName: String): StateChange? =
        stateChanges.firstOrNull {
            stateChangeName == it.name && it.fromState == currentState
        }

    fun reset() {
        currentState = startState
    }

    override fun componentType() = Companion

    companion object : SystemComponentSingleType<Workflow>(Workflow::class) {
        override fun createEmpty() = Workflow()
    }
}