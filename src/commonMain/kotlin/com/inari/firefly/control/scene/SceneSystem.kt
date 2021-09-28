package com.inari.firefly.control.scene

import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.composite.CompositeEvent
import com.inari.firefly.composite.CompositeEventListener
import com.inari.firefly.composite.CompositeEventType
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.component.ComponentMap.MapAction.*
import com.inari.firefly.core.component.ComponentMapRO
import com.inari.firefly.core.system.ComponentSystem
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.game.world.WorldSystem
import com.inari.firefly.game.world.player.PlayerEvent
import com.inari.util.Call
import com.inari.util.aspect.Aspects
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

object SceneSystem : FFSystem {

    private val activeScenes = BitSet()

    private val compositeListener: CompositeEventListener = { eType, id, compositeAspect ->
        when (eType) {
            CompositeEventType.ACTIVATED ->  when (compositeAspect) {
                Scene -> activeScenes.set(id.index, true)
                else -> {}
            }
            CompositeEventType.DEACTIVATED -> when (compositeAspect) {
                Scene -> activeScenes.set(id.index, false)
                else -> {}
            }
            CompositeEventType.DISPOSED  ->  when (compositeAspect) {
                Scene -> {
                    val scene: Scene = FFContext[id]
                    if (scene.removeAfterRun)
                        FFContext.delete(id)
                }
                else -> {}
            }
        }
    }

    private val updateListener = {
        var i = activeScenes.nextSetBit(0)
        while(i >= 0) {
            val scene: Scene = FFContext[Scene, i]
            if (scene.runTaskRef >= 0 && scene.scheduler.needsUpdate())
                TaskSystem.runTask(scene.runTaskRef, scene.componentId)
            i = activeScenes.nextSetBit(i + 1)
        }
    }

    init {
        FFContext.registerListener(CompositeEvent, compositeListener)
        FFContext.registerListener(FFApp.UpdateEvent, updateListener)
    }

    fun runScene(index: Int, callback: Call) {
        val scene = FFContext[Scene, index]
        scene.withCallback(callback)
        FFContext.activate(Scene, index)
    }

    fun pauseScene(index: Int) = FFContext.deactivate(Scene, index)
    fun resumeScene(index: Int) = FFContext.activate(Scene, index)
    fun stopScene(index: Int) = FFContext.dispose(Scene, index)


    override fun clearSystem() {
        activeScenes.clear()
    }
}