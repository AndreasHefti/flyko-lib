package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.game.composite.Composite
import com.inari.firefly.game.world.Player.PlayerEventType.*
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.event.Event
import com.inari.util.geom.GeomUtils
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField

class Player private constructor() : Composite(Player), ControlledComponent<Player> {

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
    var playerVelocity = Vector2f()
        internal set

    override fun initialize() {
        super.initialize()
        if (this.name == NO_NAME)
            throw IllegalStateException("Player component needs a unique name. Please give the player a name.")
        playerEntityKey = Entity.getKey(this.name, true)
    }

    override fun load() {
        super.load()
        val playerEntity = Entity[playerEntityKey]
        playerPosition = playerEntity[ETransform].position
        playerPivot(playerEntity[ETransform].pivot)
        playerVelocity = if (EMovement in playerEntity.aspects)
            playerEntity[EMovement].velocity
        else
            Vector2f(ZERO_FLOAT, ZERO_FLOAT)

        send(PLAYER_LOADED)
    }

    override fun activate() {
        super.activate()
        Entity.activate(playerEntityKey)
        send(PLAYER_ACTIVATED)
    }

    override fun deactivate() {
        super.deactivate()
        Entity.deactivate(playerEntityKey)
        send(PLAYER_DEACTIVATED)
    }

    override fun dispose() {
        super.dispose()
        playerEntityKey = NO_COMPONENT_KEY
        playerPosition = Vector2f()
        playerPivot = Vector2i()
        playerVelocity = Vector2f()
        send(PLAYER_DISPOSED)
    }

    class PlayerEvent(override val eventType: EventType) : Event<(PlayerEvent) -> Unit>() {

        var type: PlayerEventType = PlayerEventType.PLAYER_LOADED
            internal set
        var changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT
            internal set
        var orientation = PlayerOrientation()

        @Suppress("OVERRIDE_BY_INLINE")
        override inline fun notify(listener: (PlayerEvent) -> Unit) = listener(this)

    }

    companion object :  ComponentSubTypeSystem<Composite, Player>(Composite, "Player") {
        override fun create() = Player()

        val PLAYER_FILTER: (Composite) -> Boolean = { c -> c.componentType === this }

        @JvmField val PLAYER_ASPECT_GROUP = IndexedAspectType("PLAYER_ASPECT_GROUP")
        @JvmField val UNDEFINED_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("UNDEFINED")
        @JvmField  val PLAYER_EVENT_TYPE = Event.EventType("PlayerEvent")
        private val EVENT = PlayerEvent(PLAYER_EVENT_TYPE)
        private fun send(type: PlayerEventType, changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT) {
            EVENT.type = type
            EVENT.changeAspect = changeAspect
            Engine.notify(EVENT)
        }
    }
}

class DefaultSelectionBasedRoomSupplier: RoomSupplier {

    override fun getNextRoom(playerXPos: Int, playerYPos: Int, orientation: Orientation): PlayerOrientation {
        if (Room.activeRoomKey.instanceIndex < 0) throw IllegalStateException()
        val room = Room[Room.activeRoomKey]
        if (room.areaOrientationType != WorldOrientationType.SECTION)
            throw RuntimeException("Unsupported area orientation type: ${room.areaOrientationType}")


        val player = Player.findFirstActive()
            ?: throw IllegalStateException("No Player found")

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