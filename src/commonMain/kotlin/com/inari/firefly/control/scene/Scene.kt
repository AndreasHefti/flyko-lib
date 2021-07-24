package com.inari.firefly.control.scene

import com.inari.firefly.VOID_CALL
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.Call
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class Scene protected constructor() : SystemComponent(Scene::class.simpleName!!), TriggeredSystemComponent {

    @JvmField internal var callback: Call = VOID_CALL
    @JvmField internal var paused = false

    var removeAfterRun: Boolean = false
    
    private val pauseCall = { SceneSystem.pauseScene(index) }
    private val resumeCall = { SceneSystem.resumeScene(index) }
    private val stopCall = { SceneSystem.stopScene(index) }

    fun <A : Trigger> runTrigger(cBuilder: SystemComponentBuilder<A>, callback: Call, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = { SceneSystem.runScene(index, callback) }
        return result
    }

    fun <A : Trigger> withStopTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A  {
        val result = super.withTrigger(cBuilder, configure)
        result.call = stopCall
        return result
    }

    fun <A : Trigger> pauseTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = pauseCall
        return result
    }

    fun <A : Trigger> withResumeTrigger(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): A {
        val result = super.withTrigger(cBuilder, configure)
        result.call = resumeCall
        return result
    }


    abstract fun sceneInit()
    abstract fun sceneReset()

    internal operator fun invoke() = update()
    protected abstract fun update()

    override fun dispose() {
        super.dispose()
        disposeTrigger()
    }

    override fun componentType(): ComponentType<Scene> = Companion
    companion object : SystemComponentType<Scene>(Scene::class)
}