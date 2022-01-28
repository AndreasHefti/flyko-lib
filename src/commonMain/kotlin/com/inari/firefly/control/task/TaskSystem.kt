package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.OpResult
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.util.aspect.Aspects

object TaskSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
            Task)

    val tasks: ComponentMapRO<Task>
        get() = _tasks
    private val _tasks = ComponentSystem.createComponentMapping(
        Task, nameMapping = true
    )

    init {
        FFContext.loadSystem(this)
    }

    fun runTask(
        name: String,
        compId1: Int = -1,
        compId2: Int = -1,
        compId3: Int = -1): OpResult = TaskSystem.tasks[name](compId1, compId2, compId3)

    fun runTask(
        taskId: CompId,
        compId1: Int = -1,
        compId2: Int = -1,
        compId3: Int = -1): OpResult = TaskSystem.tasks[taskId](compId1, compId2, compId3)

    fun runTask(
        taskIndex: Int,
        compId1: Int = -1,
        compId2: Int = -1,
        compId3: Int = -1): OpResult = TaskSystem.tasks[taskIndex](compId1, compId2, compId3)

    override fun clearSystem() {
        _tasks.clear()
    }

}