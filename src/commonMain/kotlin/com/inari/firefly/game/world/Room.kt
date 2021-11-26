package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.NO_NAME
import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.control.scene.Scene
import com.inari.firefly.control.task.Task
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Room private constructor(): GenericComposite() {

    @JvmField var roomOrientationType: WorldOrientationType = WorldOrientationType.PIXELS
    @JvmField val roomOrientation: Vector4i = Vector4i()
    @JvmField val tileDimension: Vector2i = Vector2i()

    @JvmField var areaOrientationType: WorldOrientationType = WorldOrientationType.SECTION
    @JvmField val areaOrientation: Vector4i = Vector4i()

    @JvmField var activationSceneName = NO_NAME
    @JvmField var deactivationSceneName = NO_NAME
    @JvmField var pauseTaskName = NO_NAME
    @JvmField var resumeTaskName = NO_NAME

    @JvmField val withActivationScene = ComponentRefResolver(Scene) {
        activationSceneName = FFContext[Scene, it].name
    }
    fun  withActivationScene(configure: (Scene.() -> Unit)): CompId  {
        val result = Scene.buildAndGet(configure)
        activationSceneName = result.name
        return result.componentId
    }

    @JvmField val withDeactivationScene = ComponentRefResolver(Scene) {
        deactivationSceneName = FFContext[Scene, it].name
    }
    fun withDeactivationScene(configure: (Scene.() -> Unit)): CompId {
        val result = Scene.buildAndGet(configure)
        deactivationSceneName = result.name
        return result.componentId
    }

    @JvmField val withPauseTask = ComponentRefResolver(Scene) {
        pauseTaskName = FFContext[Task, it].name
    }
    fun <A : Task> withPauseTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        pauseTaskName = result.name
        return result.componentId
    }

    @JvmField val withResumeTask = ComponentRefResolver(Scene) {
        resumeTaskName = FFContext[Task, it].name
    }
    fun <A : Task> withResumeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.buildAndGet(configure)
        resumeTaskName = result.name
        return result.componentId
    }


    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Room>(Composite, Room::class) {
        init { CompositeSystem.compositeBuilderMapping[Room::class.simpleName!!] = this }
        override fun createEmpty() = Room()
    }
}