package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.graphics.view.Scene
import com.inari.util.OperationCallback
import com.inari.util.OperationResult
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

enum class WorldOrientationType {
    TILES,
    PIXELS,
    SECTION,
    COUNT
}

data class PlayerOrientation(
    @JvmField val roomKey: ComponentKey = Component.NO_COMPONENT_KEY,
    @JvmField val playerPosition: Vector2f = Vector2f()
)

interface RoomSupplier {
    fun getNextRoom(playerXPos: Int, playerYPos: Int, orientation: Orientation): PlayerOrientation
}

class Area private constructor() : Composite(Area) {

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Vector4i = Vector4i()

    companion object : ComponentSubTypeBuilder<Composite, Area>(Composite,"Area") {
        override fun create() = Area()
    }
}

class Room private constructor() : Composite(Room) {

    @JvmField var roomOrientationType: WorldOrientationType = WorldOrientationType.PIXELS
    @JvmField val roomOrientation: Vector4i = Vector4i()
    @JvmField val tileDimension: Vector2i = Vector2i()

    @JvmField var areaOrientationType: WorldOrientationType = WorldOrientationType.SECTION
    @JvmField val areaOrientation: Vector4i = Vector4i()

    @JvmField val activationScene = CReference(Scene)
    @JvmField val deactivationScene = CReference(Scene)
    @JvmField val pauseTask = CReference(Task)
    @JvmField val resumeTask = CReference(Task)

    @JvmField var roomSupplier: RoomSupplier = DefaultSelectionBasedRoomSupplier()

    override fun activate() {
        activateRoom(this.index)
        super.activate()
    }

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

    fun withPauseTask(configure: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(configure)
        pauseTask(key)
        return key
    }

    fun withResumeTask(configure: (Task.() -> Unit)): ComponentKey {
        val key = Task.build(configure)
        resumeTask(key)
        return key
    }

    @ThreadLocal
    companion object : ComponentSubTypeBuilder<Composite, Room>(Composite,"Room") {
        override fun create() = Room()

        var paused = false
            internal set

        var activeRoomKey = NO_COMPONENT_KEY
            private set

        private fun activateRoom(roomIndex: Int) {
            // If this room is already active, ignore
            if (activeRoomKey.instanceIndex == roomIndex) return
            // If there is another active room, deactivate it first
            deactivateRoom()

            // set new active room and notify
            activeRoomKey = Room.getKey(roomIndex)
        }

        fun pauseRoom() {
            if (paused || activeRoomKey == NO_COMPONENT_KEY) return

            val activeRoom = Room[activeRoomKey]
            if (activeRoom.pauseTask.exists)
                Task[activeRoom.pauseTask](activeRoom.key)
            paused = true
        }

        fun startRoom(playerIndex: Int, px: Int, py: Int, roomIndex: Int) {
            val startGameCall: OperationCallback = { resumeRoom() }
            // activate new room and player
            Room.activate(roomIndex)
            val activeRoom = Room[activeRoomKey]
            Player.activate(playerIndex)
            val player = Player[playerIndex]
            player.playerPosition(px, py)

            pauseRoom()

            // run play init scene if defined or start game directly
            if (activeRoom.activationScene.defined)
                Scene.runScene(activeRoom.activationScene.targetKey.instanceIndex, startGameCall)
            else
                startGameCall(OperationResult.SUCCESS)
        }

        fun resumeRoom() {
            if (!paused || activeRoomKey == NO_COMPONENT_KEY) return

            val activeRoom = Room[activeRoomKey]
            if (activeRoom.resumeTask.exists)
                Task[activeRoom.resumeTask](activeRoom.key)
            paused = false
        }

        fun deactivateRoom() {
            if (activeRoomKey == NO_COMPONENT_KEY) return
            Room.deactivate(activeRoomKey)
            activeRoomKey = NO_COMPONENT_KEY
        }

        fun handleRoomChange(playerXPos: Int, playerYPos: Int, orientation: Orientation = Orientation.NONE) {
            if (activeRoomKey.instanceIndex < 0)
                throw IllegalStateException("No active Room")

            val activeRoom = Room[activeRoomKey]
            handleRoomChange(activeRoom.roomSupplier.getNextRoom(playerXPos, playerYPos, orientation))
        }

        private fun handleRoomChange(playerOrientation: PlayerOrientation) {
             if (playerOrientation.roomKey.instanceIndex < 0)
                throw IllegalStateException("No Room found")
            if (activeRoomKey.instanceIndex < 0)
                throw IllegalStateException("No active Room")

            val player = Player.findFirstActive()
            pauseRoom()
            val activeRoom = Room[activeRoomKey]
            val startGameCallback: OperationCallback = {
                resumeRoom()
            }
            val disposeGameCallback: OperationCallback = {

                // deactivate player and room, clear systems
                Player.deactivate(player.index)
                deactivateRoom()

                // activate new room and player
                Room.activate(playerOrientation.roomKey.instanceIndex)
                player.playerPosition(playerOrientation.playerPosition)
                Player.activate(player.index)
                pauseRoom()

                // run play init scene if defined or start game directly
                if (activeRoom.activationScene.exists)
                    Scene.runScene(activeRoom.activationScene, startGameCallback)
                else
                    startGameCallback(OperationResult.SUCCESS)
            }

            if (activeRoom.deactivationScene.exists)
                Scene.runScene(activeRoom.deactivationScene, disposeGameCallback)
            else
                disposeGameCallback(OperationResult.SUCCESS)
        }
    }
}

