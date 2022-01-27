package com.inari.firefly.control.ai.state

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.core.component.CompId
import com.inari.util.Consumer
import com.inari.util.event.Event

class WorkflowEvent(override val eventType: EventType) : Event<Consumer<WorkflowEvent>>() {

    enum class Type {
        WORKFLOW_STARTED,
        STATE_CHANGED,
        WORKFLOW_FINISHED
    }

    lateinit var type: Type
        internal set
    var workflowId: CompId = NO_COMP_ID
        internal set
    var workflowName = NO_NAME
        internal set
    var stateChangeName = NO_NAME
        internal set
    var fromName = NO_NAME
        internal set
    var toName = NO_NAME
        internal set

    override fun notify(listener: Consumer<WorkflowEvent>) = listener(this)

    companion object : EventType("WorkflowEvent") {
        private val workflowEvent = WorkflowEvent(this)
        fun send(
            type: Type,
            workflowId: CompId,
            stateChangeName: String = NO_NAME,
            fromName: String = NO_NAME,
            toName: String = NO_NAME
        ) {
            workflowEvent.type = type
            workflowEvent.workflowId = workflowId
            workflowEvent.workflowName = StateSystem.workflows[workflowId].name
            workflowEvent.stateChangeName = stateChangeName
            workflowEvent.fromName = fromName
            workflowEvent.toName = toName
            FFContext.notify(workflowEvent)
        }

        fun send(
            type: Type,
            workflowId: CompId,
            stateChange: StateChange
        ) {
            workflowEvent.type = type
            workflowEvent.workflowId = workflowId
            workflowEvent.workflowName = StateSystem.workflows[workflowId].name
            workflowEvent.stateChangeName = stateChange.name
            workflowEvent.fromName = stateChange.fromState
            workflowEvent.toName = stateChange.toState
            FFContext.notify(workflowEvent)
        }
    }
}