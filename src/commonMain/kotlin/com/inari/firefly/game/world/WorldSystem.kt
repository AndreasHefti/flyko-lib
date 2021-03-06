@file:Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")

package com.inari.firefly.game.world

import com.inari.firefly.*
import com.inari.firefly.composite.*
import com.inari.firefly.control.scene.SceneSystem
import com.inari.firefly.control.task.TaskSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.FFSystem
import com.inari.firefly.game.player.PlayerEvent
import com.inari.firefly.game.player.PlayerEventListener
import com.inari.firefly.game.player.PlayerEventType
import com.inari.firefly.game.player.PlayerSystem
import com.inari.util.Call
import com.inari.util.Consumer
import com.inari.util.collection.BitSet
import com.inari.util.geom.*
import kotlin.jvm.JvmField
import kotlin.math.floor

object WorldSystem : FFSystem {

    var nextRoomSupplier: NextRoomSupplier = DEFAULT_SECTION_BASED_ROOM_SUPPLIER

    var activeWorldId = NO_COMP_ID
        private set
    var activeAreaId = NO_COMP_ID
        private set
    var activeRoomId = NO_COMP_ID
        private set

    internal val worlds = BitSet()
    internal val areas = BitSet()
    internal val rooms = BitSet()

    private var paused = false
    private val compositeListener: CompositeEventListener = { eType, id, compositeAspect ->
        when (eType) {
            CompositeEventType.CREATED -> when (compositeAspect) {
                World -> worlds[id.index] = true
                Area -> areas[id.index] = true
                Room -> rooms[id.index] = true
                else -> DO_NOTHING
            }
            CompositeEventType.LOADED -> when (compositeAspect) {
                World -> WorldEvent.send(WorldEventType.WORLD_LOADED, id)
                Area -> AreaEvent.send(AreaEventType.AREA_LOADED, id)
                Room -> RoomEvent.send(RoomEventType.ROOM_LOADED, id)
                else -> DO_NOTHING
            }
            CompositeEventType.ACTIVATED ->  when (compositeAspect) {
                World -> activateWorld(id)
                Area -> activateArea(id)
                Room -> activateRoom(id)
                else -> DO_NOTHING
            }
            CompositeEventType.DEACTIVATED ->  when (compositeAspect) {
                World -> deactivateWorld(id)
                Area -> deactivateArea(id)
                Room -> deactivateRoom(id)
                else -> DO_NOTHING
            }
            CompositeEventType.DISPOSED -> when (compositeAspect) {
                World -> WorldEvent.send(WorldEventType.WORLD_DISPOSED, id)
                Area -> AreaEvent.send(AreaEventType.AREA_DISPOSED, id)
                Room -> RoomEvent.send(RoomEventType.ROOM_DISPOSED, id)
                else -> DO_NOTHING
            }
            CompositeEventType.DELETED -> when (compositeAspect) {
                World -> worlds[id.index] = false
                Area -> areas[id.index] = false
                Room -> rooms[id.index] = false
                else -> DO_NOTHING
            }
        }
    }

    init {
        FFContext.registerListener(CompositeEvent, compositeListener)
        FFContext.registerListener(PlayerEvent, RoomPlayerListener.playerEventListener)
    }

    fun forEachWorld(consumer: Consumer<World>) = FFContext.forEach(World, worlds, consumer)
    fun forEachArea(consumer: Consumer<Area>) = FFContext.forEach(Area, areas, consumer)
    fun forEachRoom(consumer: Consumer<Room>) = FFContext.forEach(Room, rooms, consumer)

    fun pauseRoom() {
        if (activeRoomId != NO_COMP_ID && !paused) {
            val room = FFContext[Room, activeRoomId]
            if (room.pauseTaskName != NO_NAME)
                TaskSystem.runTask(room.pauseTaskName, room.index)
            paused = true
            RoomEvent.send(RoomEventType.ROOM_PAUSED, activeRoomId)
        }
    }

    fun resumeRoom() {
        if (activeRoomId != NO_COMP_ID && paused) {
            val room = FFContext[Room, activeRoomId]
            if (room.resumeTaskName != NO_NAME)
                TaskSystem.runTask(room.resumeTaskName, room.index)
            paused = false
            RoomEvent.send(RoomEventType.ROOM_RESUMED, activeRoomId)
        }
    }

    private fun activateWorld(worldId: CompId) {

        // If there is another active world, deactivate it first
        if (activeWorldId != NO_COMP_ID)
            FFContext.deactivate(World, worldId)

        // set new active world and notify
        activeWorldId = worldId
        WorldEvent.send(WorldEventType.WORLD_ACTIVATED, worldId)
    }

    private fun deactivateWorld(worldId: CompId) {
        if (activeWorldId == NO_COMP_ID)
            return

        // Just reset the active world reference and notify
        activeWorldId = NO_COMP_ID
        WorldEvent.send(WorldEventType.WORLD_DEACTIVATED, worldId)
    }

    private fun activateArea(areaId: CompId) {

        // If there is another active area, deactivate it first
        if (activeAreaId != NO_COMP_ID)
            FFContext.deactivate(Area, areaId)

        // set new active area and notify
        activeAreaId = areaId
        AreaEvent.send(AreaEventType.AREA_ACTIVATED, areaId)
    }

    private fun deactivateArea(areaId: CompId) {
        if (activeAreaId == NO_COMP_ID)
            return

        // Just reset the active area reference and notify
        activeAreaId = NO_COMP_ID
        AreaEvent.send(AreaEventType.AREA_DEACTIVATED, areaId)
    }

    private fun activateRoom(roomId: CompId) {

        // If there is another active room, deactivate it first
        if (activeRoomId != NO_COMP_ID)
            FFContext.deactivate(Room, roomId)

        // set new active room and notify
        activeRoomId = roomId
        RoomEvent.send(RoomEventType.ROOM_ACTIVATED, roomId)
    }

    private fun deactivateRoom(roomId: CompId) {
        if (activeRoomId == NO_COMP_ID)
            return

        // Just reset the active room reference and notify
        activeRoomId = NO_COMP_ID
        RoomEvent.send(RoomEventType.ROOM_DEACTIVATED, roomId)
    }

    fun startRoom(px: Int, py: Int) = ComponentRefResolver(Room) {
        val startGame: Call = { resumeRoom() }
        // activate new room and player
        FFContext.activate(Room, it)
        val activeRoom = FFContext[Room, activeRoomId]
        PlayerSystem.playerPosition(px, py)
        PlayerSystem.activatePlayer()
        pauseRoom()

        // run play init scene if defined or start game directly
        if (activeRoom.activationSceneName != NO_NAME)
            SceneSystem.runScene(startGame)(activeRoom.activationSceneName)
        else
            startGame()
    }

    private fun handleRoomChange(px: Int, py: Int, orientation: Orientation = Orientation.NONE) =
        handleRoomChange(nextRoomSupplier(px, py, orientation))

    private fun handleRoomChange(nextRoomKey: RoomKey) {
        if (nextRoomKey.roomId == NO_COMP_ID)
            throw IllegalStateException("No Room found")

        pauseRoom()
        val activeRoom = FFContext[Room, activeRoomId]
        val startGame: Call = { resumeRoom() }
        val disposeGame: Call = {

            // deactivate player and room, clear systems
            PlayerSystem.deactivatePlayer()
            FFContext.deactivate(Room, activeRoomId)

            // activate new room and player
            FFContext.activate(Room, nextRoomKey.roomId)
            PlayerSystem.playerPosition(
                nextRoomKey.playerPositionX,
                nextRoomKey.playerPositionY)
            PlayerSystem.activatePlayer()
            pauseRoom()

            // run play init scene if defined or start game directly
            if (activeRoom.activationSceneName != NO_NAME)
                SceneSystem.runScene(startGame)(activeRoom.activationSceneName)
            else
                startGame()
        }

        if (activeRoom.deactivationSceneName != NO_NAME)
            SceneSystem.runScene(disposeGame)(activeRoom.deactivationSceneName)
        else
            disposeGame()
    }

    override fun clearSystem() {
        worlds.clear()
        areas.clear()
        rooms.clear()

        activeWorldId = NO_COMP_ID
        activeAreaId = NO_COMP_ID
        activeRoomId = NO_COMP_ID
    }

    internal object RoomPlayerListener {

        @JvmField internal var roomX1 = -1
        @JvmField internal var roomX2 = -1
        @JvmField internal var roomY1 = -1
        @JvmField internal var roomY2 = -1

        @JvmField internal val playerEventListener: PlayerEventListener = { event ->
            when (event.type) {
                PlayerEventType.PLAYER_ACTIVATED -> {
                    // set room coordinates relative to the origin of player position
                    val room = FFContext[Room, activeRoomId]
                    roomX1 = room.roomOrientation.x
                    roomX2 = room.roomOrientation.x + room.roomOrientation.width
                    roomY1 = room.roomOrientation.y
                    roomY2 = room.roomOrientation.y + room.roomOrientation.height
                    if (room.roomOrientationType == WorldOrientationType.TILES) {
                        roomX1 *= room.tileDimension.v0
                        roomX2 *= room.tileDimension.v0
                        roomY1 *= room.tileDimension.v1
                        roomY2 *= room.tileDimension.v1
                    }

                    FFContext.registerListener(FFApp.UpdateEvent, updateListener)
                }

                PlayerEventType.PLAYER_EXIT -> handleRoomChange(event.room)

                PlayerEventType.PLAYER_DEACTIVATED -> {
                    FFContext.disposeListener(FFApp.UpdateEvent, updateListener)
                    roomX1 = -1
                    roomX2 = -1
                    roomY1 = -1
                    roomY2 = -1
                }
                else -> DO_NOTHING
            }
        }

        private val updateListener: Call = call@ {
            if (paused)
                return@call

            val px = floor(PlayerSystem.playerPosition.x).toInt()
            val py = floor(PlayerSystem.playerPosition.y).toInt()
            val ppx = px + PlayerSystem.playerPivot.x
            val ppy = py + PlayerSystem.playerPivot.y

            var orientation = Orientation.NONE
            if (ppx < roomX1) {
                orientation = Orientation.WEST
            } else if (ppx > roomX2) {
                orientation = Orientation.EAST
            } else if (ppy < roomY1) {
                orientation = Orientation.NORTH
            } else if (ppy > roomY2) {
                orientation = Orientation.SOUTH
            }

            if (orientation != Orientation.NONE)
                handleRoomChange(ppx, ppy, orientation)
        }
    }
}