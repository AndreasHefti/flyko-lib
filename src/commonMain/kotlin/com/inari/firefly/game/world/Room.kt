package com.inari.firefly.game.world

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

    @JvmField internal var activationSceneRef = -1
    @JvmField internal var deactivationSceneRef = -1
    @JvmField internal var pauseTaskRef = -1
    @JvmField internal var resumeTaskRef = -1

    @JvmField val withActivationScene = ComponentRefResolver(Scene) { activationSceneRef = it }
    fun  withActivationScene(configure: (Scene.() -> Unit)): CompId  {
        val result = Scene.build(configure)
        activationSceneRef = result.instanceId
        return result
    }

    @JvmField val withDeactivationScene = ComponentRefResolver(Scene) { deactivationSceneRef = it }
    fun withDeactivationScene(configure: (Scene.() -> Unit)): CompId {
        val result = Scene.build(configure)
        deactivationSceneRef = result.instanceId
        return result
    }


    @JvmField val withPauseTask = ComponentRefResolver(Scene) { pauseTaskRef = it }
    fun <A : Task> withPauseTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        pauseTaskRef = result.instanceId
        return result
    }

    @JvmField val withResumeTask = ComponentRefResolver(Scene) { resumeTaskRef = it }
    fun <A : Task> withResumeTask(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        resumeTaskRef = result.instanceId
        return result
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Room>(Composite, Room::class) {
        init { CompositeSystem.compositeBuilderMapping[Room::class.simpleName!!] = this }
        override fun createEmpty() = Room()
    }
}