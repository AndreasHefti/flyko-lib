package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.core.component.CompId
import com.inari.firefly.game.player.PlayerSystem
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Orientation

typealias NextRoomSupplier = (Int, Int, Orientation) -> RoomKey
data class RoomKey(
    val worldId: CompId = NO_COMP_ID,
    val areaId: CompId = NO_COMP_ID,
    val roomId: CompId = NO_COMP_ID,
    val playerPositionX: Int = 0,
    val playerPositionY: Int = 0
)

val DEFAULT_SECTION_BASED_ROOM_SUPPLIER: NextRoomSupplier = nextRoomSupplier@ { px, py, orientation ->

    val room = FFContext[Room, WorldSystem.activeRoomId]
    if (room.areaOrientationType == WorldOrientationType.SECTION) {

        // calc section dimension
        val sWidth = room.roomOrientation.width / room.areaOrientation.width
        val sHeight = room.roomOrientation.height / room.areaOrientation.height

        // find section of player relative to room coordinates
        var sx = GeomUtils.intoBoundary(px / sWidth, 0, room.areaOrientation.width - 1)
        var sy = GeomUtils.intoBoundary(py / sHeight, 0, room.areaOrientation.height - 1)
        val sOffsetX = px % sWidth
        val sOffsetY = py % sHeight
        // set section position relative to area coordinates
        sx += room.areaOrientation.x
        sy += room.areaOrientation.y
        when (orientation) {
            // exit old room north way, enter new room on south
            Orientation.NORTH -> {
                sy--
                val newRoom = FFContext.findFirst(Room, WorldSystem.rooms) { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return@nextRoomSupplier RoomKey()
                val normalizedSx = sx - newRoom.areaOrientation.x
                RoomKey(
                    WorldSystem.activeWorldId,
                    WorldSystem.activeAreaId,
                    newRoom.componentId,
                    normalizedSx * sWidth + sOffsetX - PlayerSystem.playerPivot.v0,
                    newRoom.areaOrientation.height * sHeight - PlayerSystem.playerPivot.v1)
            }
            // exit old room east way, enter new room on west
            Orientation.EAST -> {
                sx++
                val newRoom = FFContext.findFirst(Room, WorldSystem.rooms) { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return@nextRoomSupplier RoomKey()
                val normalizedSy = sy - newRoom.areaOrientation.y
                RoomKey(
                    WorldSystem.activeWorldId,
                    WorldSystem.activeAreaId,
                    newRoom.componentId,
                    - PlayerSystem.playerPivot.v0,
                    normalizedSy * sHeight + sOffsetY - PlayerSystem.playerPivot.v1)
            }
            // exit old room south way, enter new room on north
            Orientation.SOUTH -> {
                sy++
                val newRoom = FFContext.findFirst(Room, WorldSystem.rooms) { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return@nextRoomSupplier RoomKey()
                val normalizedSx = sx - newRoom.areaOrientation.x
                RoomKey(
                    WorldSystem.activeWorldId,
                    WorldSystem.activeAreaId,
                    newRoom.componentId,
                    normalizedSx * sWidth + sOffsetX - PlayerSystem.playerPivot.v0,
                    - PlayerSystem.playerPivot.v1)
            }
            // exit old room west way, enter new room on east
            Orientation.WEST -> {
                sx--
                val newRoom = FFContext.findFirst(Room, WorldSystem.rooms) { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return@nextRoomSupplier RoomKey()
                val normalizedSy = sy - newRoom.areaOrientation.y
                RoomKey(
                    WorldSystem.activeWorldId,
                    WorldSystem.activeAreaId,
                    newRoom.componentId,
                    newRoom.areaOrientation.width * sWidth - PlayerSystem.playerPivot.v0,
                    normalizedSy * sHeight + sOffsetY - PlayerSystem.playerPivot.v1)
            }
            else -> { throw RuntimeException("No Room Found") }
        }

    } else
        throw RuntimeException("Unsupported area orientation type: ${room.areaOrientationType}")
}



