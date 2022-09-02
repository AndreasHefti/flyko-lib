package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.game.composite.Composite
import com.inari.firefly.graphics.view.Scene
import com.inari.util.geom.Vector2i
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

enum class WorldOrientationType {
    TILES,
    PIXELS,
    SECTION,
    COUNT
}

data class PlayerOrientation(
    val roomKey: ComponentKey = Component.NO_COMPONENT_KEY,
    val playerPositionX: Int = 0,
    val playerPositionY: Int = 0
)

class Area private constructor() : Composite(Area) {

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Vector4i = Vector4i()

    companion object :  ComponentSubTypeSystem<Composite, Area>(Composite, "Area") {
        override fun create() = Area()
    }
}

class Room private constructor() : Composite(Room) {

    @JvmField var roomOrientationType: WorldOrientationType = WorldOrientationType.PIXELS
    @JvmField val roomOrientation: Vector4i = Vector4i()
    @JvmField val tileDimension: Vector2i = Vector2i()

    @JvmField var areaOrientationType: WorldOrientationType = WorldOrientationType.SECTION
    @JvmField val areaOrientation: Vector4i = Vector4i()

    @JvmField val activationScene = CLooseReference(Scene)
    @JvmField val deactivationScene = CLooseReference(Scene)
    @JvmField val pauseTask = CLooseReference(Task)
    @JvmField val resumeTask = CLooseReference(Task)

    @JvmField internal var paused = false

    fun  withActivationScene(configure: (Scene.() -> Unit)): ComponentKey  {
        val key = Scene.build(configure)
        activationScene(key)
        return key
    }

    fun withDeactivationScene(configure: (Scene.() -> Unit)): ComponentKey {
        val key = Scene.build(configure)
        deactivationScene(key)
        return key
    }

    fun <A : Task> withPauseTask(cBuilder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val key = cBuilder.build(configure)
        pauseTask(key)
        return key
    }

    fun <A : Task> withResumeTask(cBuilder: ComponentBuilder<A>, configure: (A.() -> Unit)): ComponentKey {
        val key = cBuilder.build(configure)
        resumeTask(key)
        return key
    }

    companion object :  ComponentSubTypeSystem<Composite, Room>(Composite, "Room") {
        override fun create() = Room()

        fun pauseActiveRoom() {
            val activeRoom = findFirst { room -> room.active } ?: return
            if (activeRoom.paused) return

            if (activeRoom.pauseTask.exists)
                Task[activeRoom.pauseTask](activeRoom.index)
            activeRoom.paused = true
        }


        fun resumeRoom() {
            val activeRoom = findFirst { room -> room.active } ?: return
            if (!activeRoom.paused) return

            if (activeRoom.resumeTask.exists)
                Task[activeRoom.resumeTask](activeRoom.index)
            activeRoom.paused = false
        }
    }
}