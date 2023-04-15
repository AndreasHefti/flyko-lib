package com.inari.firefly.game.actor

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ComponentIndex
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.game.actor.Player.PlayerEventType.*
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.SimpleCameraController
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.movement.EMovement
import com.inari.util.NO_NAME
import com.inari.util.ZERO_FLOAT
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.event.Event
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

data class PlayerOrientation(
    @JvmField val roomKey: ComponentKey = Component.NO_COMPONENT_KEY,
    @JvmField val playerPosition: Vector2f = Vector2f()
)


interface PlayerCamera  {

    fun getView(): View?
    fun initBounds(x: Float = ZERO_FLOAT, y: Float = ZERO_FLOAT, width: Float, height: Float)
    fun initPlayer(playerId: ComponentIndex)
    fun adjust()
}

class SimplePlayerCamera private constructor() : SimpleCameraController(), PlayerCamera {

    override fun getView(): View? =
        if (controlledComponentKey.componentIndex > NULL_COMPONENT_INDEX)
            View[controlledComponentKey]
        else null

    override fun initBounds(x: Float, y: Float, width: Float, height: Float) =
        snapToBounds(x, y, width, height)

    override fun initPlayer(playerId: ComponentIndex) {
        val p = Player[playerId]
        this.pivot = p.playerPosition
        this.adjust()
    }

    companion object :  SubComponentBuilder<Control, SimplePlayerCamera>(Control) {
        override fun create() = SimplePlayerCamera()
    }
}

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

    @JvmField val cameraRef = CReference(Control) {
        if (it.componentIndex <= NULL_COMPONENT_INDEX)
            throw IllegalArgumentException("The PlayerCamera must exist before it can be referenced here")
        if (Control[it] !is PlayerCamera)
            throw IllegalArgumentException("The reference must be of type PlayerCamera")
    }
    fun adjustCamera() = (Control[cameraRef] as PlayerCamera).adjust()

    var playerEntityKey = NO_COMPONENT_KEY

    var playerEntity: Entity? = null
        internal set
    var playerPosition = Vector2f()
        internal set
    var playerPivot = Vector2i()
        internal set
    var playerMovement: EMovement? = null
        internal set

    override fun addToGroup(group: Aspect): Aspects {
        val groups = super.addToGroup(group)
        playerEntity?.addToGroup(group) ?: Entity[playerEntityKey].addToGroup(group)
        return groups
    }

    override fun removeFromGroup(group: Aspect): Aspects {
        val groups = super.removeFromGroup(group)
        playerEntity?.removeFromGroup(group)
        return groups
    }

    fun withPlayerEntity(config: (Entity.() -> Unit)): ComponentKey {
        playerEntityKey = Entity.build(config)
        return playerEntityKey
    }

    override fun initialize() {
        super.initialize()
        if (this.name == NO_NAME)
            throw IllegalStateException("Player component needs a unique name. Please give the player a name.")
        playerEntityKey = Entity.getOrCreateKey(this.name)
    }

    override fun load() {
        super.load()
        playerEntity = Entity[playerEntityKey]
        playerEntity!!.groups + this.groups
        playerPosition = playerEntity!![ETransform].position
        playerPivot(playerEntity!![ETransform].pivot)
        if (EMovement in playerEntity!!.aspects)
            playerMovement = playerEntity!![EMovement]

        send(index, PLAYER_LOADED)
    }

    override fun activate() {
        super.activate()
        Entity.activate(playerEntityKey)
        send(index, PLAYER_ACTIVATED)
    }

    override fun deactivate() {
        super.deactivate()
        Entity.deactivate(playerEntityKey)
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

    @ThreadLocal
    companion object : ComponentSubTypeBuilder<Composite, Player>(Composite, "Player") {
        override fun create() = Player()

        @JvmField val PLAYER_ASPECT_GROUP = IndexedAspectType("PLAYER_ASPECT_GROUP")
        @JvmField val UNDEFINED_PLAYER_ASPECT = PLAYER_ASPECT_GROUP.createAspect("UNDEFINED")
        @JvmField val PLAYER_EVENT_TYPE = Event.EventType("PlayerEvent")
        private val EVENT = PlayerEvent(PLAYER_EVENT_TYPE)
        private fun send(playerIndex: Int, type: PlayerEventType, changeAspect: Aspect = UNDEFINED_PLAYER_ASPECT) {
            EVENT.playerIndex = playerIndex
            EVENT.type = type
            EVENT.changeAspect = changeAspect
            Engine.notify(EVENT)
        }

        fun findFirstActive(): Player {
            val pIndex = activeComponentSet.nextIndex(0)
            if (pIndex < 0)
                throw IllegalStateException("No Player found")
            return Player[pIndex]
        }

        const val PLAYER_GOES_EAST_CONDITION = "PlayerGoesEast"
        const val PLAYER_GOES_WEST_CONDITION = "PlayerGoesWest"
        const val PLAYER_GOES_SOUTH_CONDITION = "PlayerGoesSouth"
        const val PLAYER_GOES_NORTH_CONDITION = "PlayerGoesNorth"
        init {
            Conditional {
                name = PLAYER_GOES_EAST_CONDITION
                condition = { playerKey, _ -> (Player[playerKey.name].playerMovement?.velocity?.x ?: 0f) > 0f }
            }
            Conditional {
                name = PLAYER_GOES_WEST_CONDITION
                condition = { playerKey, _ -> (Player[playerKey.name].playerMovement?.velocity?.x ?: 0f) < 0f }
            }
            Conditional {
                name = PLAYER_GOES_SOUTH_CONDITION
                condition = { playerKey, _ -> (Player[playerKey.name].playerMovement?.velocity?.y ?: 0f) > 0f }
            }
            Conditional {
                name = PLAYER_GOES_NORTH_CONDITION
                condition = { playerKey, _ -> (Player[playerKey.name].playerMovement?.velocity?.y ?: 0f) < 0f }
            }
        }
    }
}