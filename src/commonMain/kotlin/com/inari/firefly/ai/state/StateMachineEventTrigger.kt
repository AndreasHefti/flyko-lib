package com.inari.firefly.ai.state

import com.inari.firefly.ai.state.FiniteStateMachine.Companion.STATE_CHANGE_EVENT_TYPE
import com.inari.firefly.core.ComponentSubTypeSystem
import com.inari.firefly.core.Engine
import com.inari.firefly.core.Trigger

class StateMachineEventTrigger  private constructor() : Trigger() {

    private val stateChangeEventListener = { doTrigger() }

    override fun load() = Engine.registerListener(STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)
    override fun dispose() = Engine.disposeListener(STATE_CHANGE_EVENT_TYPE, stateChangeEventListener)

    companion object :  ComponentSubTypeSystem<Trigger, StateMachineEventTrigger>(Trigger, "StateMachineEventTrigger") {
        override fun create() = StateMachineEventTrigger()
    }

}