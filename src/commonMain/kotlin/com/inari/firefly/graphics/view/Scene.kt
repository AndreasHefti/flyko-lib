package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
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

        stopScene(index)
        callback(result)
        if (deleteAfterRun)
            Scene.delete(index)
    }

    companion object :  ComponentSubTypeSystem<Control, Scene>(Control, "Scene") {
        override fun create() = Scene()

        fun runScene(reference: CLooseReference, callback: OperationCallback) = runScene(reference.targetKey, callback)
        fun runScene(name: String, callback: OperationCallback) = runScene(Scene[name].index, callback)
        fun runScene(key: ComponentKey, callback: OperationCallback) = runScene(key.instanceIndex, callback)
        fun runScene(index: Int, callback: OperationCallback) {
            checkIndex(index)
            val scene = this[index]
            scene.withCallback(callback)
            activate(index)
        }

        fun pauseScene(index: Int) = deactivate(index)
        fun resumeScene(index: Int) = activate(index)
        fun stopScene(index: Int) = dispose(index)
    }

}