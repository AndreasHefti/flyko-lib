@file:Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")

package com.inari.firefly.game.player

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.control.task.ComponentTask
import com.inari.firefly.control.task.SimpleTask
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.entity.Entity
import com.inari.firefly.graphics.ETransform
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.geom.PositionF
import kotlin.jvm.JvmField

object PlayerSystem : FFSystem {

    const val PLAYER_NAME = "FF_PLAYER"
    const val PLAYER_ENTITY_NAME = "FF_PLAYER_ENTITY"

    @JvmField val PLAYER_ASPECT_GROUP = IndexedAspectType("PLAYER_ASPECT_GROUP")
    @JvmField val UNDEFINED_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("UNDEFINED")

    @JvmField internal var loadTaskRef = -1
    @JvmField internal var activationTaskRef = -1
    @JvmField internal var deactivationTaskRef = -1
    @JvmField internal var unloadTaskRef = -1

    val playerComposite = PlayerComposite()

    val withPlayerLoadTask = ComponentRefResolver(SimpleTask) { index -> loadTaskRef + index }
    fun <A : SimpleTask> withPlayerLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        loadTaskRef = result.instanceId
        return result
    }

    val withPlayerActivationTask = ComponentRefResolver(SimpleTask) { index -> activationTaskRef + index }
    fun <A : SimpleTask> withPlayerActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        activationTaskRef = result.instanceId
        return result
    }

    val withPlayerDeactivationTask = ComponentRefResolver(SimpleTask) { index -> deactivationTaskRef + index }
    fun <A : SimpleTask> withPlayerDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        deactivationTaskRef = result.instanceId
        return result
    }

    val withPlayerUnloadTask = ComponentRefResolver(SimpleTask) { index -> unloadTaskRef + index }
    fun <A : SimpleTask> withPlayerUnloadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
         unloadTaskRef = result.instanceId
        return result
    }

    fun loadPlayer(x: Int = 0, y: Int = 0) {
        if (playerComposite.loaded)
            return                      // already loaded

        // run additional load task if available
        if (loadTaskRef >= 0)
            TaskSystem.runTask(loadTaskRef)

        // set quick references
        playerComposite.entityId = FFContext[Entity, PLAYER_ENTITY_NAME].componentId
        playerComposite.playerTransform = FFContext[ETransform, playerComposite.entityId]
        playerComposite.playerPosition = playerComposite.playerTransform.position
        // set position
        playerComposite.playerTransform.position(x, y)

        // notify
        playerComposite.loaded = true
        PlayerEvent.send(PlayerEventType.PLAYER_LOADED)
    }

    fun activatePlayer() {

        if (playerComposite.active)
            return                      // already active
        if (!playerComposite.loaded)
            loadPlayer()                // not loaded yet (load automatically)

        // run additional activation task if available
        if (activationTaskRef >= 0)
            TaskSystem.runTask(activationTaskRef)

        // activate all registered components
        if (!playerComposite.activatableComponents.isEmpty)
            FFContext.activateAll(playerComposite.activatableComponents)

        // notify
        playerComposite.active = true
        PlayerEvent.send(PlayerEventType.PLAYER_ACTIVATED)
    }

    fun deactivatePlayer() {

        if (!playerComposite.active)
            return                      // already inactive

        // deactivate all registered components
        if (!playerComposite.activatableComponents.isEmpty)
            FFContext.deactivateAll(playerComposite.activatableComponents)

        // run additional deactivation task if available
        if (deactivationTaskRef >= 0)
            TaskSystem.runTask(deactivationTaskRef)

        // notify
        playerComposite.active = false
        PlayerEvent.send(PlayerEventType.PLAYER_DEACTIVATED)
    }

    fun unloadPlayer() {

        if (!playerComposite.loaded)
            return                      // already loaded
        if (playerComposite.active)
            deactivatePlayer()          // deactivate first is still active

        // unload and clear loaded components first
        playerComposite.activatableComponents.clear()
        FFContext.deleteAllQuietly(playerComposite.loadedComponents)
        playerComposite.loadedComponents.clear()

        // exec additional unload task of defined
        if (unloadTaskRef >= 0)
            TaskSystem.runTask(unloadTaskRef)

        // clear data
        playerComposite.entityId = NO_COMP_ID
        playerComposite.playerPosition = PositionF()

        // notify
        playerComposite.loaded = false
        PlayerEvent.send(PlayerEventType.PLAYER_DELETED)
    }

    val applyPlayerTask = ComponentRefResolver(ComponentTask) { index ->
        TaskSystem.runTask(index, playerComposite.entityId)
    }


    override fun clearSystem() {

    }
}