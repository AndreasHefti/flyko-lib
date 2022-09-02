package com.inari.firefly.graphics.view

import com.inari.firefly.core.ComponentSubTypeSystem
import com.inari.firefly.core.Control
import com.inari.firefly.core.Engine
import com.inari.firefly.core.api.FFTimer
import com.inari.util.*
import kotlin.jvm.JvmField

class Scene : Control() {

    @JvmField internal var scheduler: FFTimer.Scheduler = Engine.INFINITE_SCHEDULER
    @JvmField internal var updateOperation: Operation = RUNNING_OPERATION
    @JvmField internal var callback: OperationCallback = VOID_CONSUMER
    @JvmField var deleteAfterRun: Boolean = false

    fun withCallback(callback: OperationCallback) {
        this.callback = callback
    }

    fun withUpdate(update: Operation) {
        updateOperation = update
    }

    override fun update() {
        if (!scheduler.needsUpdate())
            return

        val result = updateOperation()
        if (result == OperationResult.RUNNING)
            return

        stop(index)
        callback(result)
        if (deleteAfterRun)
            Scene.delete(index)
    }

    companion object :  ComponentSubTypeSystem<Control, Scene>(Control, "Scene") {
        override fun create() = Scene()

        fun run(index: Int, callback: OperationCallback) {
            val scene = this[index]
            scene.withCallback(callback)
            activate(index)
        }

        fun pause(index: Int) = deactivate(index)
        fun resume(index: Int) = activate(index)
        fun stop(index: Int) = dispose(index)
    }

}