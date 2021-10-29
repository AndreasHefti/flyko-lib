package com.inari.firefly.control.state

import com.inari.firefly.*
import com.inari.firefly.control.scene.Scene
import com.inari.firefly.control.scene.SceneSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.api.FFTimer
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.util.Call
import com.inari.util.aspect.Aspects


object StateSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(Workflow)

    val workflows: ComponentMapRO<Workflow>
        get() = systemWorkflows
    private val systemWorkflows = ComponentSystem.createComponentMapping(
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

    private fun update() {
        var j = 0
        while (j < systemWorkflows.map.capacity) {
            val workflow = systemWorkflows.map[j++] ?: continue
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
        val workflow = systemWorkflows[workflowId]
        doStateChange(workflow, workflow.findStateChangeForCurrentState(stateChangeName)!!)
    }

    fun changeState(workflowId: Int, targetStateName: String) {
        val workflow = systemWorkflows[workflowId]
        doStateChange(workflow, workflow.findStateChangeForTargetState(targetStateName)!!)
    }

    fun createChangeToStateCall(workflowId: Int, targetStateName: String): Call = {
        changeState(workflowId, targetStateName)
    }

    fun createStateChangeCall(workflowId: Int, stateChangeName: String): Call = {
        changeState(workflowId, stateChangeName)
    }

    private fun doStateChange(workflow: Workflow, stateChange: StateChange) {
        if (stateChange.disposeStateTaskRef != -1)
            TaskSystem.runTask(stateChange.disposeStateTaskRef, workflow.componentId)

        workflow.currentState = stateChange.toState

        if (stateChange.initStateTaskRef != -1)
            TaskSystem.runTask(stateChange.initStateTaskRef, workflow.componentId)

        if (stateChange.toState !== NO_STATE)
            WorkflowEvent.send(
                WorkflowEvent.Type.STATE_CHANGED,
                workflow.componentId,
                stateChange
            )
        else
            WorkflowEvent.send(
                WorkflowEvent.Type.WORKFLOW_FINISHED,
                workflow.componentId,
                stateChange
            )
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
        systemWorkflows.clear()
    }
}