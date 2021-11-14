package com.inari.firefly.control.trigger

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.NULL_CALL
import com.inari.firefly.control.state.Workflow
import com.inari.firefly.control.state.WorkflowEvent
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.rendering.Renderer
import com.inari.util.Call
import com.inari.util.Consumer
import kotlin.jvm.JvmField

class StateChangeTrigger private constructor(): Trigger() {

    enum class Type {
        STATE_CHANGE,
        ENTER_STATE,
        EXIT_STATE
    }

    private var workflowRef = -1
    private val listener : Consumer<WorkflowEvent> =  listener@ { event ->

            if (event.workflowId.instanceId != workflowRef)
                return@listener

            when (this.type) {
                Type.STATE_CHANGE ->
                    if (event.type === WorkflowEvent.Type.STATE_CHANGED && typeName == event.stateChangeName)
                        doTrigger()
                Type.ENTER_STATE ->
                    if ((event.type === WorkflowEvent.Type.STATE_CHANGED ||
                                event.type === WorkflowEvent.Type.WORKFLOW_STARTED) &&
                        typeName == event.toName)
                        doTrigger()
                Type.EXIT_STATE ->
                    if ((event.type === WorkflowEvent.Type.STATE_CHANGED ||
                        event.type === WorkflowEvent.Type.WORKFLOW_FINISHED) &&
                        typeName == event.fromName)
                        doTrigger()
            }

    }

    @JvmField var type : Type = Type.STATE_CHANGE
    @JvmField val workflow = ComponentRefResolver(Workflow) { index -> workflowRef = index }
    @JvmField var typeName : String  = NO_NAME

    override fun init() {
        super.init()
        FFContext.registerListener(WorkflowEvent, listener)
    }

    override fun dispose() {
        FFContext.disposeListener(WorkflowEvent, listener)
        super.dispose()
    }

    override fun componentType() = Renderer
    companion object : SystemComponentSubType<Trigger, StateChangeTrigger>(Trigger, StateChangeTrigger::class) {
        override fun createEmpty() = StateChangeTrigger()
    }
}