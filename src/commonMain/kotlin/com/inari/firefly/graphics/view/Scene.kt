package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.util.*
import kotlin.jvm.JvmField

class Scene private constructor(): Control() {

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
            Scene.system.delete(index)
    }

    companion object : ComponentSubTypeBuilder<Control, Scene>(Control,"Scene") {
        override fun create() = Scene()

        fun runScene(reference: CReference, callback: OperationCallback) = runScene(reference.targetKey, callback)
        fun runScene(name: String, callback: OperationCallback) = runScene(Scene.system[name].index, callback)
        fun runScene(key: ComponentKey, callback: OperationCallback) = runScene(key.instanceIndex, callback)
        fun runScene(index: Int, callback: OperationCallback) {
            checkIndex(index)
            val scene = system[index] as Scene
            scene.withCallback(callback)
            activate(index)
        }

        fun pauseScene(index: Int) = deactivate(index)
        fun resumeScene(index: Int) = activate(index)
        fun stopScene(index: Int) = dispose(index)
    }
}