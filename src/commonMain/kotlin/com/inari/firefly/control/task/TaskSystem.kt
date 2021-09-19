package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.state.StateSystem
import com.inari.firefly.control.state.Workflow
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.game.world.World
import com.inari.util.OpResult
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object TaskSystem : ComponentSystem {

    override val supportedComponents: Aspects = SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(
            SimpleTask,
            GenericTask,
            ComponentTask)

    val simpleTasks: ComponentMapRO<SimpleTask>
        get() = _simpleTasks
    private val _simpleTasks = ComponentSystem.createComponentMapping(
        SimpleTask, nameMapping = true
    )

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

    val runTask = ComponentRefResolver(SimpleTask) { index ->
        _simpleTasks[index].invoke()
    }

    fun runTask(
        name: String,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[name](compId1, compId2, compId3, compId4, compId5)

    fun runTask(
        taskId: CompId,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[taskId](compId1, compId2, compId3, compId4, compId5)

    fun runTask(
        taskIndex: Int,
        compId1: CompId,
        compId2: CompId = NO_COMP_ID,
        compId3: CompId = NO_COMP_ID,
        compId4: CompId = NO_COMP_ID,
        compId5: CompId = NO_COMP_ID): OpResult = componentTasks[taskIndex](compId1, compId2, compId3, compId4, compId5)

    override fun clearSystem() {
        _genericTasks.clear()
        _componentTasks.clear()
    }
}