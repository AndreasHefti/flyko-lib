package com.inari.firefly.control.scene

import com.inari.firefly.VOID_CALL
import com.inari.firefly.control.trigger.Trigger
import com.inari.firefly.control.trigger.TriggeredSystemComponent
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.Call
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField

abstract class Scene protected constructor() : TriggeredSystemComponent(Scene::class.simpleName!!) {

    @JvmField internal var callback: Call = VOID_CALL
    @JvmField internal var paused = false

    var removeAfterRun: Boolean = false

    private val triggerIds = BitSet()
    private val pauseCall = { SceneSystem.pauseScene(index) }
    private val resumeCall = { SceneSystem.resumeScene(index) }
    private val stopCall = { SceneSystem.stopScene(index) }

    fun <A : Trigger> runTrigger(cBuilder: Trigger.Subtype<A>, callback: Call, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, { SceneSystem.runScene(index, callback) }, configure)

    fun <A : Trigger> stopTrigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, stopCall, configure)

    fun <A : Trigger> pauseTrigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, pauseCall, configure)

    fun <A : Trigger> resumeTrigger(cBuilder: Trigger.Subtype<A>, configure: (A.() -> Unit)): A =
        super.trigger(cBuilder, resumeCall, configure)

    abstract fun sceneInit()
    abstract fun sceneReset()

    internal operator fun invoke() = update()
    protected abstract fun update()

    override fun componentType(): ComponentType<Scene> = Companion
    companion object : SystemComponentType<Scene>(Scene::class)
}