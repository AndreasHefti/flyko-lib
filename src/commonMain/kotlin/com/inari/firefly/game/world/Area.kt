package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.game.actor.player.Player
import com.inari.firefly.game.room.ERoomTransition
import com.inari.firefly.game.room.Room
import com.inari.firefly.game.room.RoomCamera
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.ContactCallback
import com.inari.firefly.physics.contact.FullContactScan
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector2f
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

class Area private constructor() : Composite(Area) {

    @JvmField val viewRef = CReference(View)
    @JvmField val cameraRef = CReference(RoomCamera)
    @JvmField val loadScene = CReference(Scene)
    @JvmField val disposeScene = CReference(Scene)
    @JvmField var loadInParallel = true
    @JvmField var stopLoadSceneWhenLoadFinished = true
    @JvmField var activateAfterLoadScene = true

    private var loadTaskRunning = false
    private var loadSceneRunning = false

    override fun load() {
        if (loadTaskRunning) return

        loadTaskRunning = true

        // if there is a load scene, run it
        if (loadScene.exists) {
            if (loadInParallel) {
                // start load in new thread then start scene to run in parallel
                println("startParallelTask")
                startParallelTask("Area Load Task", { super.load() }) {
                    if (stopLoadSceneWhenLoadFinished) {
                        Scene.stopScene(loadScene.refIndex)
                        loadSceneRunning = false
                    }
                    loadTaskRunning = false
                }
                if (loadScene.exists) {
                    loadSceneRunning = true
                    Scene.runScene(loadScene) { _, result ->
                        if (result == OperationResult.SUCCESS && activateAfterLoadScene)
                            Area.activate(this)
                        loadSceneRunning = false
                    }
                }
            } else {
                // load first then show scene
                super.load()
                loadTaskRunning = false
                if (loadScene.exists) {
                    loadSceneRunning = true
                    Scene.runScene(loadScene) { _, result ->
                        if (result == OperationResult.SUCCESS && activateAfterLoadScene)
                            Area.activate(this)
                        loadSceneRunning = false
                    }
                }
            }
        } else {
            super.load()
            loadSceneRunning = false
        }
    }

    @ThreadLocal
    companion object : ComponentSubTypeBuilder<Composite, Area>(Composite,"Area") {
        override fun create() = Area()

        @JvmField var roomTransitionCallback: ContactCallback = ::applyRoomTransition

        fun applyRoomTransition(playerEntityKey: ComponentKey, scan: FullContactScan): Boolean {
            val entity = Entity[scan.getFirstContact(Room.ROOM_TRANSITION_CONTACT_TYPE).entityId]
            val transition = entity[ERoomTransition]
            if (ConditionalComponent[transition.condition](playerEntityKey)) {
                val room = Room.getActiveForPlayer(playerEntityKey.name)
                    ?: return false

                if (room.deactivationScene.exists) {
                    Scene[room.deactivationScene].callback = { _, _ ->
                        loadNextRoom(playerEntityKey.name, entity, transition)
                        Room.dispose(room)
                    }
                    Room.deactivate(room)
                } else {
                    Room.deactivate(room)
                    loadNextRoom(playerEntityKey.name, entity, transition)
                    Room.dispose(room)
                }
                return true
            }
            return false
        }

        private val playerToTransition = Vector2f()
        private val playerTargetPos = Vector2f()
        private fun loadNextRoom(playerName: String, entity: Entity, transition: ERoomTransition) {

            val newRoom = Room[transition.targetRoom]
            val player = Player[playerName]

            newRoom.withPlayer(player)
            Room.activate(newRoom)

            val targetEntity = Entity[transition.targetTransition]
            val targetTransform = targetEntity[ETransform]
            val sourceTransform = entity[ETransform]
            playerToTransition(player.playerPosition) - sourceTransform.position
            playerTargetPos(targetTransform.position) + playerToTransition
            player.playerPosition(playerTargetPos )

            when (transition.orientation) {
                Orientation.EAST ->  player.playerPosition.x += 5f
                Orientation.WEST ->  player.playerPosition.x -= 5f
                Orientation.NORTH ->  player.playerPosition.y -= 5f
                Orientation.SOUTH ->  player.playerPosition.y += 5f
                else -> {}
            }

            RoomCamera[Area[newRoom.areaRef].cameraRef].adjust()
        }
    }
}