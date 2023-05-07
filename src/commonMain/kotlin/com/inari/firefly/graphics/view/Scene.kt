package com.inari.firefly.graphics.view

import com.inari.firefly.core.*
import com.inari.firefly.core.api.*
import com.inari.firefly.core.api.ActionResult.RUNNING
import com.inari.util.VOID_CONSUMER_2
import com.inari.util.collection.AttributesRO
import kotlin.jvm.JvmField

class Scene private constructor(): Control() {

    @JvmField val attributes = AttributesRO.EMPTY_ATTRIBUTES
    @JvmField val loadTask = CReference(Task)
    @JvmField val disposeTask = CReference(Task)
    @JvmField var initAction = VOID_ACTION
    @JvmField var updateAction = VOID_ACTION
    @JvmField var callback = NO_ACTION_CALLBACK
    @JvmField var deleteAfterRun = false

    init {
        autoActivation = false
    }

    fun withCallback(callback: ActionCallback) {
        this.callback = callback
    }

    fun withUpdate(update: Action) {
        updateAction = update
    }

    fun withLoadTask(config: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(config)
        loadTask(key)
        return key
    }

    fun withDisposeTask(config: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(config)
        disposeTask(key)
        return key
    }

    override fun load() {
        super.load()

        if (loadTask.exists)
            Task[loadTask](this.key, attributes)
    }

    override fun activate() {
        super.activate()
        initAction(this.key)
    }

    override fun dispose() {
        if (disposeTask.exists)
            Task[disposeTask](this.key, attributes)

        super.dispose()
    }

    override fun update() {
        if (!scheduler.needsUpdate())
            return

        val result = updateAction(key)
        if (result == RUNNING)
            return

        stopScene(index)
        callback(key, result)
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
            val scene = Scene[index]
            scene.withCallback(callback)
            activate(index)
        }

        fun pauseScene(index: Int) = deactivate(index)
        fun resumeScene(index: Int) = activate(index)
        fun stopScene(index: Int) = dispose(index)
    }
}