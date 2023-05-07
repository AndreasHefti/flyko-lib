package com.inari.firefly.game.world

import com.inari.firefly.core.*
import com.inari.firefly.core.api.ActionResult
import com.inari.firefly.game.actor.Player
import com.inari.firefly.game.actor.PlayerCamera
import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.graphics.view.Scene
import com.inari.firefly.graphics.view.View
import com.inari.firefly.physics.contact.ContactCallback
import com.inari.firefly.physics.contact.FullContactScan
import com.inari.util.NO_NAME
import com.inari.util.collection.Attributes
import com.inari.util.geom.Orientation
import com.inari.util.geom.Vector2f
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

class Area private constructor() : Composite(Area) {

    @JvmField val loadScene = CReference(Scene)
    @JvmField val disposeScene = CReference(Scene)
    @JvmField var loadInParallel = true
    @JvmField var stopLoadSceneWhenLoadFinished = true
    @JvmField var activateAfterLoadScene = true

    @JvmField var roomViewName = NO_NAME
    @JvmField var roomCameraName = NO_NAME
    @JvmField var roomActivationSceneName: String = NO_NAME
    @JvmField var roomDeactivationSceneName: String = NO_NAME


    private var loadTaskRunning = false
    private var loadSceneRunning = false

    fun withRoomLoadTask(attributes: Attributes, taskName: String) {
        withLifecycleTask {
            this.attributes = attributes
            lifecycleType = LifecycleTaskType.ON_LOAD
            task(taskName)
        }
    }

    fun withRoomLoadTask(attributes: Attributes, task: Task) =
        withRoomLoadTask(attributes, task.key)

    fun withRoomLoadTask(attributes: Attributes, taskKey: ComponentKey) {
        withLifecycleTask {
            this.attributes = attributes
            lifecycleType = LifecycleTaskType.ON_LOAD
            task(taskKey)
        }
    }

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
                        if (result == ActionResult.SUCCESS && activateAfterLoadScene)
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
                        if (result == ActionResult.SUCCESS && activateAfterLoadScene)
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
                        activateNextRoom(playerEntityKey.name, entity, transition)
                        Room.dispose(room)
                    }
                    Room.deactivate(room)
                } else {
                    Room.deactivate(room)
                    activateNextRoom(playerEntityKey.name, entity, transition)
                    Room.dispose(room)
                }
                return true
            }
            return false
        }

        fun getDefaultView(areaKey: ComponentKey): View? {
            if (areaKey == NO_COMPONENT_KEY)
                return null
            val area = Area[areaKey]
            if (area.roomViewName == NO_NAME)
                return getDefaultCamera(areaKey)?.getView()
            return View[area.roomViewName]
        }

        fun getDefaultCamera(areaKey: ComponentKey): PlayerCamera? {
            if (areaKey == NO_COMPONENT_KEY)
                return null
            val area = Area[areaKey]
            if (area.roomCameraName == NO_NAME)
                return null
            return Control[area.roomCameraName] as PlayerCamera
        }

        private val playerToTransition = Vector2f()
        private val playerTargetPos = Vector2f()
        private fun activateNextRoom(playerName: String, entity: Entity, transition: ERoomTransition) {

            val newRoom = Room[transition.targetRoom]
            val player = Player[playerName]

            newRoom.playerRef(player)
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

            player.adjustCamera()
        }
    }
}