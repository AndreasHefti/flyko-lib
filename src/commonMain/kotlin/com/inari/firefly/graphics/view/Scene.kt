package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.Action
import com.inari.firefly.core.api.ActionCallback
import com.inari.firefly.core.api.OperationResult.*
import com.inari.firefly.core.api.RUNNING_ACTION
import com.inari.util.*
import kotlin.jvm.JvmField

class Scene private constructor(): Control() {

    @JvmField internal var updateOperation: Action = RUNNING_ACTION
    @JvmField internal var callback: ActionCallback = VOID_CONSUMER_2
    @JvmField var deleteAfterRun: Boolean = false

    fun withCallback(callback: ActionCallback) {
        this.callback = callback
    }

    fun withUpdate(update: Action) {
        updateOperation = update
    }

    override fun update() {
        if (!scheduler.needsUpdate())
            return

        val result = updateOperation(index)
        if (result == RUNNING)
            return

        stopScene(index)
        callback(index, result)
        if (deleteAfterRun)
            Scene.system.delete(index)
    }

    companion object : ComponentSubTypeBuilder<Control, Scene>(Control,"Scene") {
        override fun create() = Scene()

        fun runScene(reference: CReference, callback: ActionCallback) = runScene(reference.targetKey, callback)
        fun runScene(name: String, callback: ActionCallback) = runScene(Scene.system[name].index, callback)
        fun runScene(key: ComponentKey, callback: ActionCallback) = runScene(key.componentIndex, callback)
        fun runScene(index: Int, callback: ActionCallback) {
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