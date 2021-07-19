package com.inari.firefly.control.task

import com.inari.firefly.FFContext
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.entity.Entity
import com.inari.firefly.entity.EntitySystem
import com.inari.util.OpResult
import com.inari.util.aspect.Aspects
import kotlin.jvm.JvmField

object TaskSystem : ComponentSystem {

    override val supportedComponents: Aspects =
        SystemComponent.SYSTEM_COMPONENT_ASPECTS.createAspects(SystemTask, EntityTask)

    @JvmField val systemTasks = ComponentSystem.createComponentMapping(
        SystemTask, nameMapping = true
    )

    @JvmField val entityTasks = ComponentSystem.createComponentMapping(
            EntityTask, nameMapping = true
    )

    init {
        FFContext.loadSystem(this)
    }

    fun runSystemTask(name: String) =
        runSystemTask(systemTasks.indexForName(name))

    fun runSystemTask(taskId: CompId) =
        runSystemTask(taskId.checkType(SystemTask).instanceId)

    fun runSystemTask(taskIndex: Int) {
        if (taskIndex in systemTasks) {
            systemTasks[taskIndex].task()
            if (systemTasks[taskIndex].removeAfterRun)
                systemTasks.delete(taskIndex)
        }
    }

    fun runEntityTask(name: String, entityId: Int): OpResult =
            runEntityTask(systemTasks.indexForName(name), entityId)

    fun runEntityTask(name: String, entityName: String): OpResult =
            runEntityTask(
                    systemTasks.indexForName(name),
                    EntitySystem[entityName].index)

    fun runEntityTask(taskId: CompId, entityId: CompId): OpResult =
            runEntityTask(
                    taskId.checkType(SystemTask).instanceId,
                    entityId.checkType(Entity).instanceId)

    fun runEntityTask(taskIndex: Int, entityId: Int): OpResult =
            entityTasks[taskIndex].task(entityId)

    override fun clearSystem() {
        systemTasks.clear()
        entityTasks.clear()
    }
}