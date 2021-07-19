package com.inari.firefly.control.state

import com.inari.firefly.*
import com.inari.firefly.control.state.Workflow.StateChange
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.Call
import com.inari.util.Named
import com.inari.util.aspect.Aspects
import com.inari.util.indexed.Indexed
import kotlin.jvm.JvmField


object StateSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Workflow)

    @JvmField val workflows = ComponentSystem.createComponentMapping(
        Workflow,
        activationMapping = true,
        nameMapping = true,
        listener = { workflow, action -> when (action) {
            ACTIVATED     -> activated(workflow)
            DEACTIVATED   -> deactivated(workflow)
            else -> {}
        } }
    )

    var scheduler: FFTimer.UpdateScheduler = FFContext.timer.createUpdateScheduler(10f)
        internal set(value) { scheduler = value }

    var updateResolution: Float
        set(value) { scheduler = FFContext.timer.createUpdateScheduler(value) }
        get() { throw UnsupportedOperationException() }

    init {
        FFContext.registerListener(FFApp.UpdateEvent) {
            if (scheduler.needsUpdate())
                update()
        }

        FFContext.loadSystem(this)
    }

    operator fun get(workflowIndex: Int): String =
        workflows[workflowIndex].currentState

    operator fun get(workflowIndex: Indexed): String =
        workflows[workflowIndex.index].currentState

    operator fun get(workflowId: CompId): String =
        workflows[workflowId].currentState

    operator fun get(workflowName: String): String =
        workflows[workflowName].currentState

    operator fun get(workflowName: Named): String =
        workflows[workflowName.name].currentState

    private fun update() {
        var j = 0
        while (j < workflows.map.capacity) {
            val workflow = workflows.map[j++] ?: continue
            var i = 0
            while (i < workflow.currentStateChanges.capacity) {
                val st = workflow.currentStateChanges[i++] ?: continue
                if (st.condition()) {
                    doStateChange(workflow, st)
                    break
                }
            }
        }
    }

    fun doStateChange(workflowId: Int, stateChangeName: String) {
        val workflow = workflows[workflowId]
        doStateChange(workflow, workflow.findStateChangeForCurrentState(stateChangeName)!!)
    }

    fun changeState(workflowId: Int, targetStateName: String) {
        val workflow = workflows[workflowId]
        doStateChange(workflow, workflow.findStateChangeForTargetState(targetStateName)!!)
    }

    fun createChangeToStateCall(workflowId: Int, targetStateName: String): Call = {
        changeState(workflowId, targetStateName)
    }

    fun createStateChangeCall(workflowId: Int, stateChangeName: String): Call = {
        changeState(workflowId, stateChangeName)
    }

    private fun doStateChange(workflow: Workflow, stateChange: StateChange) {
        workflow.currentState = stateChange.to

        if (stateChange.to !== NO_STATE) {
            WorkflowEvent.send(
                WorkflowEvent.Type.STATE_CHANGED,
                workflow.componentId,
                stateChange
            )
        } else {
            WorkflowEvent.send(
                WorkflowEvent.Type.WORKFLOW_FINISHED,
                workflow.componentId,
                stateChange
            )
        }
    }

    private fun activated(workflow: Workflow) {
        workflow.currentState = workflow.startState
        WorkflowEvent.send(
            WorkflowEvent.Type.WORKFLOW_STARTED,
            workflow.componentId
        )
    }

    private fun deactivated(workflow: Workflow) {
        workflow.reset()
        WorkflowEvent.send(
            WorkflowEvent.Type.WORKFLOW_FINISHED,
            workflow.componentId
        )
    }

    override fun clearSystem() {
        workflows.clear()
    }
}