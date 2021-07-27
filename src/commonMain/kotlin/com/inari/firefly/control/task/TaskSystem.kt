package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.state.StateSystem
import com.inari.firefly.control.state.Workflow
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.util.OpResult
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object TaskSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(GenericTask, ComponentTask)

    val genericTasks: ComponentMapRO<GenericTask>
        get() = _genericTasks
    private val _genericTasks = ComponentSystem.createComponentMapping(
        GenericTask, nameMapping = true
    )

    val componentTasks: ComponentMapRO<ComponentTask>
        get() = _componentTasks
    private val _componentTasks = ComponentSystem.createComponentMapping(
        ComponentTask, nameMapping = true
    )

    init {
        FFContext.loadSystem(this)
    }

    fun runTask(name: String): OpResult =
        runTask(genericTasks.indexForName(name))

    fun runTask(taskId: CompId): OpResult =
        runTask(taskId.instanceId)

    fun runTask(taskIndex: Int): OpResult {
        return if (taskIndex in _genericTasks)
            _genericTasks[taskIndex].invoke()
        else
            OpResult.FAILED
    }

    fun runTask(
        name: String,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[name].invoke(compId1, compId2, compId3, compId4, compId5)

    fun runTask(
        taskId: CompId,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[taskId].invoke(compId1, compId2, compId3, compId4, compId5)

    fun runTask(
        taskIndex: Int,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[taskIndex].invoke(compId1, compId2, compId3, compId4, compId5)

    override fun clearSystem() {
        _genericTasks.clear()
        _componentTasks.clear()
    }
}