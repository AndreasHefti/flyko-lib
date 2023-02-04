package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.game.world.Player.PlayerEventType.*
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.NO_NAME
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.event.Event
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField
import kotlin.math.floor

class Player private constructor() : Composite(Player), Controlled {

    enum class PlayerEventType {
        PLAYER_LOADED,
        PLAYER_ACTIVATED,
        PLAYER_ENTRY,
        PLAYER_CHANGED,
        PLAYER_EXIT,
        PLAYER_DEACTIVATED,
        PLAYER_DISPOSED
    }

    var playerEntityKey = NO_COMPONENT_KEY

    var playerPosition = Vector2f()
        internal set
    var playerPivot = Vector2i()
        internal set
    var playerMovement: EMovement? = null
        internal set

    //override val controllerReferences = ControllerReferences(Player)
    internal val playerRoomTransitionObserver = RoomTransitionObserver(this)

    override fun initialize() {
        super.initialize()
        if (this.name == NO_NAME)
            throw IllegalStateException("Player component needs a unique name. Please give the player a name.")
        playerEntityKey = Entity.getOrCreateKey(this.name)
    }

    override fun load() {
        super.load()
        val playerEntity = Entity[playerEntityKey]
        playerPosition = playerEntity[ETransform].position
        playerPivot(playerEntity[ETransform].pivot)
        if (EMovement in playerEntity.aspects)
            playerMovement = playerEntity[EMovement]


        send(index, PLAYER_LOADED)
    }

    override fun activate() {
        super.activate()
        Entity.activate(playerEntityKey)
        playerRoomTransitionObserver.activate()
        send(index, PLAYER_ACTIVATED)
    }

    override fun deactivate() {
        super.deactivate()
        Entity.deactivate(playerEntityKey)
        playerRoomTransitionObserver.deactivate()
        send(index, PLAYER_DEACTIVATED)
    }

    override fun dispose() {
        super.dispose()
        playerEntityKey = NO_COMPONENT_KEY
        playerPosition = Vector2f()
        playerPivot = Vector2i()
        playerMovement = null
        send(index, PLAYER_DISPOSED)
    }

    class PlayerEvent(override val eventType: EventType) : Event<(PlayerEvent) -> Unit>() {

        var playerIndex = -1
        var type: PlayerEventType = PLAYER_LOADED
            internal set
        var changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT
            internal set
        var orientation = PlayerOrientation()

        @Suppress("OVERRIDE_BY_INLINE")
        override inline fun notify(listener: (PlayerEvent) -> Unit) = listener(this)

    }

    companion object : ComponentSubTypeBuilder<Composite, Player>(Composite,"Player") {
        override fun create() = Player()

        @JvmField val PLAYER_ASPECT_GROUP = IndexedAspectType("PLAYER_ASPECT_GROUP")
        @JvmField val UNDEFINED_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("UNDEFINED")
        @JvmField  val PLAYER_EVENT_TYPE = Event.EventType("PlayerEvent")
        private val EVENT = PlayerEvent(PLAYER_EVENT_TYPE)
        private fun send(playerIndex: Int, type: PlayerEventType, changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT) {
            EVENT.playerIndex = playerIndex
            EVENT.type = type
            EVENT.changeAspect = changeAspect
            Engine.notify(EVENT)
        }

        fun findFirstActive(): Player {
            val pIndex = Player.activeIndexIterator().next()
            if (pIndex < 0)
                throw IllegalStateException("No Player found")
            return Player[pIndex]
        }
    }

    internal class RoomTransitionObserver(private val player: Player) {

        @JvmField internal var roomX1 = NULL_COMPONENT_INDEX
        @JvmField internal var roomX2 = NULL_COMPONENT_INDEX
        @JvmField internal var roomY1 = NULL_COMPONENT_INDEX
        @JvmField internal var roomY2 = NULL_COMPONENT_INDEX

        internal fun activate() {
            // set room coordinates relative to the origin of player position
            val room = Room[Room.activeRoomKey]
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

            Engine.registerListener(Engine.UPDATE_EVENT_TYPE, ::updateListener)
        }

        internal fun deactivate() {
            Engine.disposeListener(Engine.UPDATE_EVENT_TYPE, ::updateListener)
            roomX1 = NULL_COMPONENT_INDEX
            roomX2 = -NULL_COMPONENT_INDEX
            roomY1 = NULL_COMPONENT_INDEX
            roomY2 = NULL_COMPONENT_INDEX
        }

        private fun updateListener() {
            if (Room.paused)
                return

            val px = floor(player.playerPosition.x).toInt()
            val py = floor(player.playerPosition.y).toInt()
            val ppx = px + player.playerPivot.x
            val ppy = py + player.playerPivot.y

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
                Room.handleRoomChange(ppx, ppy, orientation)
        }
    }
}

class DefaultSelectionBasedRoomSupplier: RoomSupplier {

    override fun getNextRoom(playerXPos: Int, playerYPos: Int, orientation: Orientation): PlayerOrientation {
        if (Room.activeRoomKey.componentIndex < 0) throw IllegalStateException()
        val room = Room[Room.activeRoomKey]
        if (room.areaOrientationType != WorldOrientationType.SECTION)
            throw RuntimeException("Unsupported area orientation type: ${room.areaOrientationType}")

        val player = Player.findFirstActive()
        // calc section dimension
        val sWidth = room.roomOrientation.width / room.areaOrientation.width
        val sHeight = room.roomOrientation.height / room.areaOrientation.height

        // find section of player relative to room coordinates
        var sx = GeomUtils.intoBoundary(playerXPos / sWidth, 0, room.areaOrientation.width - 1)
        var sy = GeomUtils.intoBoundary(playerYPos / sHeight, 0, room.areaOrientation.height - 1)
        val sOffsetX = playerXPos % sWidth
        val sOffsetY = playerYPos % sHeight
        // set section position relative to area coordinates
        sx += room.areaOrientation.x
        sy += room.areaOrientation.y
        when (orientation) {
            // exit old room north way, enter new room on south
            Orientation.NORTH -> {
                sy--
                val newRoom = Room.findFirst { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return PlayerOrientation()
                val normalizedSx = sx - newRoom.areaOrientation.x
                return PlayerOrientation(
                    newRoom.key,
                    Vector2f(
                        normalizedSx * sWidth + sOffsetX - player.playerPivot.v0,
                        newRoom.areaOrientation.height * sHeight - player.playerPivot.v1
                    )
                )
            }
            // exit old room east way, enter new room on west
            Orientation.EAST -> {
                sx++
                val newRoom = Room.findFirst { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return PlayerOrientation()
                val normalizedSy = sy - newRoom.areaOrientation.y
                return PlayerOrientation(
                    newRoom.key,
                    Vector2f(
                        -player.playerPivot.v0,
                        normalizedSy * sHeight + sOffsetY - player.playerPivot.v1
                    )
                )
            }
            // exit old room south way, enter new room on north
            Orientation.SOUTH -> {
                sy++
                val newRoom = Room.findFirst { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return PlayerOrientation()
                val normalizedSx = sx - newRoom.areaOrientation.x
                return PlayerOrientation(
                    newRoom.key,
                    Vector2f(
                        normalizedSx * sWidth + sOffsetX - player.playerPivot.v0,
                        -player.playerPivot.v1
                    )
                )
            }
            // exit old room west way, enter new room on east
            Orientation.WEST -> {
                sx--
                val newRoom = Room.findFirst { r ->
                    GeomUtils.contains(r.areaOrientation, sx, sy)
                } ?: return PlayerOrientation()
                val normalizedSy = sy - newRoom.areaOrientation.y
                return PlayerOrientation(
                    newRoom.key,
                    Vector2f(
                        newRoom.areaOrientation.width * sWidth - player.playerPivot.v0,
                        normalizedSy * sHeight + sOffsetY - player.playerPivot.v1
                    )
                )
            }
            else -> throw RuntimeException("No Room Found")
        }
    }
}