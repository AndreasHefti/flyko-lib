package com.inari.firefly.control.trigger

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.NULL_CALL
import com.inari.firefly.control.state.Workflow
import com.inari.firefly.control.state.WorkflowEvent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.util.Call
import com.inari.util.Consumer

class StateChangeTrigger private constructor(): Trigger() {

    enum class Type {
        STATE_CHANGE,
        ENTER_STATE,
        EXIT_STATE
    }

    private var workflowRef = -1
    private var call: Call = NULL_CALL
    private val listener : Consumer<WorkflowEvent> =  listener@ { event ->

            if (event.workflowId.instanceId != workflowRef)
                return@listener

            when (this.type) {
                Type.STATE_CHANGE ->
                    if (event.type === WorkflowEvent.Type.STATE_CHANGED && typeName == event.stateChangeName)
                        doTrigger(call)
                Type.ENTER_STATE ->
                    if ((event.type === WorkflowEvent.Type.STATE_CHANGED ||
                                event.type === WorkflowEvent.Type.WORKFLOW_STARTED) &&
                        typeName == event.toName)
                        doTrigger(call)
                Type.EXIT_STATE ->
                    if ((event.type === WorkflowEvent.Type.STATE_CHANGED ||
                        event.type === WorkflowEvent.Type.WORKFLOW_FINISHED) &&
                        typeName == event.fromName)
                        doTrigger(call)
            }

    }

    var type : Type = Type.STATE_CHANGE
    val workflow = ComponentRefResolver(Workflow) { index -> workflowRef = index }
    var typeName : String  = NO_NAME


    override fun register(call: Call) {
        this.call = call
        FFContext.registerListener(WorkflowEvent, listener)
    }

    override fun dispose() {
        FFContext.disposeListener(WorkflowEvent, listener)
        call = NULL_CALL
        super.dispose()
    }

    companion object : Trigger.Subtype<StateChangeTrigger>() {
        override fun createEmpty() = StateChangeTrigger()
    }
}