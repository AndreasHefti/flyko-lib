package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.*
import com.inari.firefly.game.actor.Player
import com.inari.firefly.game.actor.Player.Companion.PLAYER_GOES_EAST_CONDITION
import com.inari.firefly.game.actor.Player.Companion.PLAYER_GOES_NORTH_CONDITION
import com.inari.firefly.game.actor.Player.Companion.PLAYER_GOES_SOUTH_CONDITION
import com.inari.firefly.game.actor.Player.Companion.PLAYER_GOES_WEST_CONDITION
import com.inari.firefly.game.actor.PlayerCamera
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.physics.contact.EContact
import com.inari.util.COMMA
import com.inari.util.VOID_CONSUMER_2
import com.inari.util.collection.DynArray
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

class ERoomTransition private constructor() : EntityComponent(ERoomTransition) {

    @JvmField val condition = CReference(ConditionalComponent)
    @JvmField val targetRoom = CReference(Room)
    @JvmField val targetTransition = CReference(Entity)
    @JvmField var orientation = Orientation.NONE

    override fun reset() {
        condition.reset()
        targetRoom.reset()
        targetTransition.reset()
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<ERoomTransition>("ERoomTransition") {
        override fun allocateArray() = DynArray.of<ERoomTransition>()
        override fun create() = ERoomTransition()
    }
}

class Room private constructor() : Composite(Room) {

    @JvmField val areaRef = CReference(Area)
    @JvmField val playerRef = CReference(Player)
    @JvmField val roomBounds = Vector4f()
    @JvmField val activationScene = CReference(Scene)
    @JvmField val deactivationScene = CReference(Scene)
    @JvmField var buttonPause = ButtonType.PAUSE

    private var roomLoadTaskRunning = false
    private var roomLoadSceneRunning = false

    private var started = false
    private var paused = false

    private fun getCamera(): PlayerCamera {
        if (playerRef.exists) {
            val camRef = Player[playerRef].cameraRef
            if (camRef.exists)
                return Control[camRef] as PlayerCamera
            else
                throw IllegalStateException("No cameraRef defined for player: $playerRef")
        } else throw IllegalStateException("No playerRef defined for room: ${this.key}")
    }

    override fun activate() {
        super.activate()

        if (playerRef.exists) {
            val player = Player[playerRef]
            player.addToGroup(ROOM_PAUSE_GROUP)
            val cam = getCamera()
            cam.initBounds(width = roomBounds.width, height = roomBounds.height)
            Player.activate(player)
            cam.initPlayer(player.index)
        }

        if (activationScene.exists) {
            Pausing.pause(ROOM_PAUSE_GROUP)
            Scene.runScene(this.activationScene) { _, _ -> run() }
        } else run()
    }

    private fun run() {
        Pausing.resume(ROOM_PAUSE_GROUP)
        started = true
    }

    internal fun pause() {
        if (!started || paused)
            return

        // pause room pause group
        Pausing.pause(ROOM_PAUSE_GROUP)
        paused = true

        // play pause task if defined

    }

    internal fun resume() {
        if (!started || !paused)
            return

        println("resume room")
        Pausing.resume(ROOM_PAUSE_GROUP)
        paused = false
    }

    override fun deactivate() {
        started = false
        Pausing.pause(ROOM_PAUSE_GROUP)

        if (deactivationScene.exists) {
            val originCallback = Scene[deactivationScene].callback
            Scene.runScene(this.deactivationScene) { key, res ->
                stop()
                originCallback(key, res)
                Scene[deactivationScene].callback = VOID_CONSUMER_2
            }
        }
        else stop()
    }

    private fun stop() {
        Pausing.resume(ROOM_PAUSE_GROUP)
        Player.deactivate(playerRef)
        super.deactivate()
    }


    @ThreadLocal
    companion object : ComponentSubTypeBuilder<Composite, Room>(Composite,"Room") {
        override fun create() = Room()

        @JvmField val ROOM_PAUSE_GROUP = COMPONENT_GROUP_ASPECT.createAspect("ROOM_PAUSE_GROUP")
        @JvmField val ROOM_TRANSITION_CONTACT_TYPE = EContact.CONTACT_TYPE_ASPECT_GROUP.createAspect("ROOM_TRANSITION_1")

        private val pauseUpdate = Engine.timer.createUpdateScheduler(20f)
        private val updatePause: () -> Unit = exit@ {

            if (!pauseUpdate.needsUpdate())
                return@exit

            var i = nextActiveIndex(0)
            while (i > NULL_COMPONENT_INDEX) {
                val room = Room[i]
                i =  nextActiveIndex(i + 1)

                if (!Engine.input.getDefaultDevice().buttonTyped(room.buttonPause))
                    continue

                if (room.paused) room.resume()
                else room.pause()
            }
        }

        fun getActiveForPlayer(playerName: String): Room? {
            val p = Player[playerName]
            var i = nextActiveIndex(0)
            while (i > NULL_COMPONENT_INDEX) {
                val room = Room[i]
                if (room.playerRef.targetKey.componentIndex == p.index)
                    return room
                i =  nextActiveIndex(i + 1)
            }
            return null
        }

        const val PLAYER_ROOM_TRANSITION_SCAN_CONDITION = "TransitionPlayerScan"
        const val PLAYER_ROOM_TRANSITION_EAST_CONDITION = "TransitionEast"
        const val PLAYER_ROOM_TRANSITION_WEST_CONDITION = "TransitionWest"
        const val PLAYER_ROOM_TRANSITION_SOUTH_CONDITION = "TransitionSouth"
        const val PLAYER_ROOM_TRANSITION_NORTH_CONDITION = "TransitionNorth"
        init {
            Engine.registerListener(UPDATE_EVENT_TYPE, updatePause)

            Conditional {
                name = PLAYER_ROOM_TRANSITION_SCAN_CONDITION
                condition = object : Condition {
                    override fun invoke(index: EntityIndex): Boolean {
                        val scan = EContact[index].contactScans.getFirstFullContact(ROOM_TRANSITION_CONTACT_TYPE)
                        return scan != null && scan.contactMask.cardinality > 8
                    }
                }
            }
            AndCondition {
                name = PLAYER_ROOM_TRANSITION_EAST_CONDITION
                left(PLAYER_ROOM_TRANSITION_SCAN_CONDITION)
                right(PLAYER_GOES_EAST_CONDITION)
            }
            AndCondition {
                name = PLAYER_ROOM_TRANSITION_WEST_CONDITION
                left(PLAYER_ROOM_TRANSITION_SCAN_CONDITION)
                right(PLAYER_GOES_WEST_CONDITION)
            }

            AndCondition {
                name = PLAYER_ROOM_TRANSITION_SOUTH_CONDITION
                left(PLAYER_ROOM_TRANSITION_SCAN_CONDITION)
                right(PLAYER_GOES_SOUTH_CONDITION)
            }
            AndCondition {
                name = PLAYER_ROOM_TRANSITION_NORTH_CONDITION
                left(PLAYER_ROOM_TRANSITION_SCAN_CONDITION)
                right(PLAYER_GOES_NORTH_CONDITION)
            }

            Task {
                name = ATTR_TRANSITION_BUILD_TASK
                operation = { roomKey, attributes, _ ->
                    val bounds = Vector4f()
                    bounds(attributes[ATTR_OBJECT_BOUNDS]!!)
                    val room = Room[roomKey]
                    room.withLifecycleComponent(
                        Entity {
                            name = attributes[ATTR_TRANSITION_ID]
                                ?: throw IllegalArgumentException("Missing ATTR_TRANSITION_ID")
                            autoActivation = true
                            withComponent(ETransform) {
                                viewRef(attributes[ATTR_OBJECT_VIEW]
                                    ?: throw IllegalArgumentException("Missing ATTR_OBJECT_VIEW"))
                                layerRef(attributes[ATTR_OBJECT_LAYER]
                                    ?: throw IllegalArgumentException("Missing ATTR_OBJECT_LAYER"))
                                position(bounds)
                            }
                            if (Engine.GLOBAL_VALUES.getBoolean(Engine.GLOBAL_SHOW_GIZMOS))
                                withComponent(EShape) {
                                    type = ShapeType.RECTANGLE
                                    vertices = floatArrayOf(0f,0f,bounds.width, bounds.height)
                                    fill = false
                                    color1(1f, 0f, 0f, 1f)
                                    blend = BlendMode.NORMAL_ALPHA
                                }
                            withComponent(EContact) {
                                contactBounds(0, 0, bounds.width.toInt(), bounds.height.toInt())
                                contactType = ROOM_TRANSITION_CONTACT_TYPE
                            }
                            withComponent(ERoomTransition) {
                                condition(attributes[ATTR_TRANSITION_CONDITION]
                                    ?: throw IllegalArgumentException("Missing ATTR_TRANSITION_CONDITION"))

                                val targetsVals = attributes[ATTR_TRANSITION_TARGET]?.split(COMMA)
                                    ?: throw IllegalArgumentException("Missing ATTR_TRANSITION_TARGET")

                                targetTransition(targetsVals[1])
                                targetRoom(targetsVals[0])

                                val oString = attributes[ATTR_TRANSITION_ORIENTATION]
                                    ?: Orientation.NONE.name

                                orientation = Orientation.valueOf(oString)
                            }
                        })
                }
            }
        }
    }
}






