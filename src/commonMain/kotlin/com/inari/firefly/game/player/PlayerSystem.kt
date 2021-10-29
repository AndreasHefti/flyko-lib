@file:Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")

package com.inari.firefly.game.player

import com.inari.firefly.FFContext
import com.inari.firefly.control.task.Task
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.aspect.IndexedAspectType
import kotlin.jvm.JvmField

object PlayerSystem : FFSystem {

    @JvmField val PLAYER_ASPECT_GROUP = IndexedAspectType("PLAYER_ASPECT_GROUP")
    @JvmField val UNDEFINED_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("UNDEFINED")

    @JvmField internal var loadTaskRef = -1
    @JvmField internal var activationTaskRef = -1
    @JvmField internal var deactivationTaskRef = -1
    @JvmField internal var unloadTaskRef = -1

    private val playerComposite = PlayerComposite.Builder.buildAndGet {}
    val playerEntityId get() = playerComposite.entityId
    val playerPosition get() = playerComposite.playerPosition
    val playerPivot get() = playerComposite.playerPivot
    val playerVelocity get() = playerComposite.playerVelocity

    val withPlayerLoadTask = playerComposite.withLoadTask
    fun <A : Task> withPlayerLoadTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): PlayerSystem {
        playerComposite.withLoadTask(cBuilder, configure)
        return this
    }
    val withPlayerActivationTask = playerComposite.withActivationTask
    fun <A : Task> withPlayerActivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): PlayerSystem {
        playerComposite.withActivationTask(cBuilder, configure)
        return this
    }
    val withPlayerDeactivationTask = playerComposite.withDeactivationTask
    fun <A : Task> withPlayerDeactivationTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): PlayerSystem {
        playerComposite.withDeactivationTask(cBuilder, configure)
        return this
    }
    val withPlayerDisposeTask = playerComposite.withDisposeTask
    fun <A : Task> withPlayerDisposeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): PlayerSystem {
        playerComposite.withDisposeTask(cBuilder, configure)
        return this
    }

    fun loadPlayer() = FFContext.load(playerComposite)
    fun activatePlayer() = FFContext.activate(playerComposite)
    fun deactivatePlayer() = FFContext.deactivate(playerComposite)
    fun disposePlayer() = FFContext.dispose(playerComposite)

    val applyPlayerTask = ComponentRefResolver(Task) { index ->
        FFContext.runTask(index, playerComposite.entityId)
    }

    override fun clearSystem() {
        disposePlayer()
    }
}