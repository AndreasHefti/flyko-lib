package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.game.composite.Composite
import com.inari.firefly.game.world.Room.Companion.activeRoomKey
import com.inari.firefly.graphics.view.Scene
import com.inari.util.OperationCallback
import com.inari.util.OperationResult
import com.inari.util.geom.*
import kotlin.jvm.JvmField

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

    @JvmField var roomSupplier: RoomSupplier = DefaultSelectionBasedRoomSupplier()

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

    companion object :  ComponentSubTypeSystem<Composite, Room>(Composite, "Room") {
        override fun create() = Room()

        var activeRoomKey = NO_COMPONENT_KEY
            private set

        fun activateRoom(roomIndex: Int) {
            // If this room is already active, ignore
            if (activeRoomKey.instanceIndex == roomIndex) return
            // If there is another active room, deactivate it first
            deactivateRoom()

            // set new active room and notify
            Room.activate(roomIndex)
            activeRoomKey = Room.getKey(roomIndex)
        }

        fun pauseRoom() {
            if (activeRoomKey == NO_COMPONENT_KEY) return

            val activeRoom = Room[activeRoomKey]
            if (activeRoom.pauseTask.exists)
                Task[activeRoom.pauseTask](activeRoom.index)
            activeRoom.paused = true
        }

        fun startRoom(playerIndex: Int, px: Int, py: Int, roomIndex: Int) {
            val startGameCall: OperationCallback = { resumeRoom() }
            // activate new room and player
            activateRoom(roomIndex)
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
            if (activeRoomKey == NO_COMPONENT_KEY) return

            val activeRoom = Room[activeRoomKey]
            if (activeRoom.resumeTask.exists)
                Task[activeRoom.resumeTask](activeRoom.index)
            activeRoom.paused = false
        }

        fun deactivateRoom() {
            if (activeRoomKey == NO_COMPONENT_KEY) return
            Room.deactivate(activeRoomKey)
            activeRoomKey = NO_COMPONENT_KEY
        }

        private fun handleRoomChange(playerXPos: Int, playerYPos: Int, orientation: Orientation = Orientation.NONE) {
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
                ?: throw IllegalStateException("No Player found")

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
                activateRoom(playerOrientation.roomKey.instanceIndex)
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

