package com.inari.firefly.game.world

import com.inari.firefly.DO_NOTHING
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
        var sx = px / sWidth
        var sy = py / sHeight
        val sOffsetX = px % sWidth
        val sOffsetY = py % sHeight
        // set section position relative to area coordinates
        sx += room.areaOrientation.x
        sy += room.areaOrientation.y
        // find room for matching section in area
        val newRoom = FFContext.findFirst(Room, WorldSystem.rooms) { r ->
            GeomUtils.contains(r.areaOrientation, sx, sy)
        } ?: return@nextRoomSupplier RoomKey()
        // find matching section relative to the room
        sx -= newRoom.areaOrientation.x
        sy -= newRoom.areaOrientation.y
        // find new player position for new room and section
        var newPx = 0
        var newPy = 0
        when (orientation) {
            // exit old room north way, enter new room on south
            Orientation.NORTH -> {
                newPx = sx * sWidth + sOffsetX
                newPy = newRoom.areaOrientation.height * sHeight + PlayerSystem.playerPivot.v1
            }
            // exit old room east way, enter new room on west
            Orientation.EAST -> {
                newPx = - PlayerSystem.playerPivot.v0
                newPy = sy * sHeight + sOffsetY
            }
            // exit old room south way, enter new room on north
            Orientation.SOUTH -> {
                newPx = sx * sWidth + sOffsetX
                newPy = - PlayerSystem.playerPivot.v1
            }
            // exit old room west way, enter new room on east
            Orientation.WEST -> {
                newPx = newRoom.areaOrientation.width * sWidth + PlayerSystem.playerPivot.v0
                newPy = sy * sHeight + sOffsetY
            }
            else -> DO_NOTHING
        }
        // assemble new room key
        RoomKey(WorldSystem.activeWorldId, WorldSystem.activeAreaId, newRoom.componentId, newPx, newPy)

    } else
        throw RuntimeException("Unsupported area orientation type: ${room.areaOrientationType}")
}



