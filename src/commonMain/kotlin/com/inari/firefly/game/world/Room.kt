package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.*
import com.inari.firefly.game.actor.Player
import com.inari.firefly.game.*
import com.inari.firefly.graphics.shape.EShape
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.SimpleCameraController
import com.inari.firefly.physics.contact.EContact
import com.inari.util.COMMA
import com.inari.util.VOID_CONSUMER_2
import com.inari.util.collection.BitSet
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

class ERoomTransition private constructor() : EntityComponent(ERoomTransition) {

    @JvmField val condition = CReference(ConditionalComponent)
    @JvmField val targetRoom = CReference(Room)
    @JvmField val targetTransition = CReference(Entity)
    @JvmField val cameraRef = CReference(RoomCamera)
    @JvmField var orientation = Orientation.NONE


    override fun reset() {
        condition.reset()
        targetRoom.reset()
        targetTransition.reset()
        cameraRef.reset()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<ERoomTransition>("ERoomTransition") {
        override fun create() = ERoomTransition()
    }
}

class RoomCamera private constructor() : SimpleCameraController() {

    internal fun initPlayer(playerId: Int) {
        val p = Player[playerId]
        this.pivot = p.playerPosition
        this.adjust()
    }

    companion object : ComponentSubTypeBuilder<Control, RoomCamera>(Control, "RoomCamera") {
        override fun create() = RoomCamera()
    }

}

class Room private constructor() : Composite(Room) {

    @JvmField val areaRef = CReference(Area)
    @JvmField val roomBounds = Vector4f()
    @JvmField val activationScene = CReference(Scene)
    @JvmField val deactivationScene = CReference(Scene)
    @JvmField var buttonPause = ButtonType.PAUSE

    private val players = BitSet(0)

    private var roomLoadTaskRunning = false
    private var roomLoadSceneRunning = false

    private var started = false
    private var paused = false

    fun withPlayer(player: Player) = withPlayer(player.key)
    fun withPlayer(name: String) = withPlayer(Player[name])
    fun withPlayer(key: ComponentKey) {
        if (key.componentIndex == NULL_COMPONENT_INDEX)
            return

        players.set(key.componentIndex)
        val player = Player[key]
        player.addToGroup(ROOM_PAUSE_GROUP)
    }

    override fun activate() {
        super.activate()

        var cam: RoomCamera? = null
        if (areaRef.exists) {
            val area = Area[areaRef]
            if (area.cameraRef.exists) {
                cam = RoomCamera[Area[areaRef].cameraRef]
                cam.snapToBounds(0f, 0f, roomBounds.width, roomBounds.height)
            }
        }

        var pi = players.nextIndex(0)
        while (pi >= 0) {
            Player.activate(pi)
            cam?.initPlayer(pi)
            pi = players.nextIndex(pi + 1)
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

        println("pause room")
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
        var pi = players.nextIndex(0)
        while (pi >= 0) {
            Player.deactivate(pi)
            pi = players.nextIndex(pi + 1)
        }
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
                if (room.players[p.index])
                    return room
                i =  nextActiveIndex(i + 1)
            }
            return null
        }



        init {
            Engine.registerListener(UPDATE_EVENT_TYPE, updatePause)

            Task {
                name = NAME_DEFAULT_TRANSITION_BUILD_TASK
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
                                    color(1f, 0f, 0f, 1f)
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






