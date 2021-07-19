package com.inari.firefly.control.state

import com.inari.firefly.FALSE_SUPPLIER
import com.inari.firefly.NO_STATE
import com.inari.firefly.core.component.ArrayAccessor
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.util.BooleanSupplier
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField


class Workflow private constructor() : SystemComponent(Workflow::class.simpleName!!) {

    @JvmField internal val int_states: DynArray<String> = DynArray.of()
    @JvmField internal val int_stateChanges: DynArray<StateChange> = DynArray.of()
    @JvmField internal val currentStateChanges: DynArray<StateChange> = DynArray.of()

    var startState = NO_STATE
    val states = ArrayAccessor(int_states)
    val stateChanges = ArrayAccessor(int_stateChanges)

    var currentState: String = NO_STATE
        internal set(value) {
            field = value
            currentStateChanges.clear()
            int_stateChanges.forEach { st ->
                if (st.from == currentState)
                    currentStateChanges.add(st)
            }
        }

    fun findStateChangeForTargetState(targetStateName: String): StateChange? =
        int_stateChanges.firstOrNull {
            it.from == currentState && targetStateName == it.to
        }


    fun findStateChangeForCurrentState(stateChangeName: String): StateChange? =
        int_stateChanges.firstOrNull {
            stateChangeName == it.name && it.from == currentState
        }

    fun reset() {
        currentState = startState
    }

    override fun componentType(): ComponentType<Workflow> = Companion
    companion object : SystemComponentSingleType<Workflow>(Workflow::class) {
        override fun createEmpty() = Workflow()
    }

    data class StateChange(
        val name: String,
        val from: String,
        val to: String,
        val condition: BooleanSupplier = FALSE_SUPPLIER
    )
}