package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.game.composite.Composite
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import com.inari.util.event.Event
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
        PLAYER_DELETED
    }

    val playerEntityRef = CLooseReference(Entity)

    var playerPosition = Vector2f()
        internal set
    var playerPivot = Vector2i()
        internal set
    var playerVelocity = Vector2f()
        internal set

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