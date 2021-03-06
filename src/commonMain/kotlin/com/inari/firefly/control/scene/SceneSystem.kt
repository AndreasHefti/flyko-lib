package com.inari.firefly.control.scene

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFApp
import com.inari.firefly.FFContext
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeEvent
import com.inari.firefly.composite.CompositeEventListener
import com.inari.firefly.composite.CompositeEventType
import com.inari.firefly.control.OpResult
import com.inari.firefly.control.invoke
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.FFSystem
import com.inari.util.Call
import com.inari.util.collection.BitSet

object SceneSystem : FFSystem {

    private val activeScenes = BitSet()

    private val compositeListener: CompositeEventListener = { eType, id, compositeAspect ->
        when (eType) {
            CompositeEventType.ACTIVATED ->  when (compositeAspect) {
                Scene -> {
                    activeScenes.set(id.index, true)
                    val scene = Scene[id.index]
                    if (scene.activateTaskRef >= 0)
                        TaskSystem.runTask(scene.activateTaskRef, scene.index)
                }
                else -> DO_NOTHING
            }
            CompositeEventType.DEACTIVATED -> when (compositeAspect) {
                Scene -> {
                    activeScenes.set(id.index, false)
                    val scene = Scene[id.index]
                    if (scene.deactivateTaskRef >= 0)
                        TaskSystem.runTask(scene.deactivateTaskRef, scene.index)
                    scene.callback()
                    if (scene.removeAfterRun)
                        FFContext.delete(scene)
                }
                else -> DO_NOTHING
            }
            CompositeEventType.DISPOSED  ->  when (compositeAspect) {
                Scene -> {
                    val scene: Scene = FFContext[id]
                    if (scene.removeAfterRun)
                        FFContext.delete(id)
                }
                else -> DO_NOTHING
            }
            else -> DO_NOTHING
        }
    }

    private val updateListener = {
        var i = activeScenes.nextSetBit(0)
        while(i >= 0) {
            val scene = Scene[i]
            if (scene.scheduler.needsUpdate())
                if (scene.update() != OpResult.RUNNING)
                    FFContext.deactivate(scene)
            i = activeScenes.nextSetBit(i + 1)
        }
    }

    init {
        FFContext.registerListener(CompositeEvent, compositeListener)
        FFContext.registerListener(FFApp.UpdateEvent, updateListener)
    }

    fun runScene(callback: Call): ComponentRefResolver<Composite> {
        return ComponentRefResolver(Scene) { index ->
            val scene: Scene = FFContext[Scene, index]
            scene.withCallback(callback)
            FFContext.activate(Scene, index)
        }
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