package com.inari.firefly.ai.state

import com.inari.firefly.ai.state.FiniteStateMachine.Companion.STATE_CHANGE_EVENT_TYPE
import com.inari.firefly.core.*

class StateMachineEventTrigger  private constructor() : Trigger(StateMachineEventTrigger) {

    private val stateChangeEventListener = { doTrigger() }

    override fun load() = Engine.registerListener(STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)
    override fun dispose() = Engine.disposeListener(STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)

    companion object : ComponentSubTypeBuilder<Trigger, StateMachineEventTrigger>(Trigger,"StateMachineEventTrigger") {
        override fun create() = StateMachineEventTrigger()
    }
}