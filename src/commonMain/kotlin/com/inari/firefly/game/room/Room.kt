package com.inari.firefly.game.room

import com.inari.firefly.core.*
import com.inari.firefly.core.Engine.Companion.UPDATE_EVENT_TYPE
import com.inari.firefly.core.api.ButtonType
import com.inari.firefly.core.api.InputDevice
import com.inari.firefly.core.api.NULL_COMPONENT_INDEX
import com.inari.firefly.core.api.OperationResult
import com.inari.firefly.game.actor.player.Player
import com.inari.firefly.graphics.view.Scene
import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator
import com.inari.util.sleepCurrentThread
import com.inari.util.startParallelTask
import kotlin.jvm.JvmField
import kotlin.native.concurrent.ThreadLocal

class Room private constructor() : Composite(Room) {

    @JvmField val loadScene = CReference(Scene)
    @JvmField val activationScene = CReference(Scene)
    @JvmField val deactivationScene = CReference(Scene)
    @JvmField val disposeScene = CReference(Scene)

    @JvmField var inputDevice: InputDevice = Engine.input.getDefaultDevice()
    @JvmField var buttonPause = ButtonType.PAUSE

    private val players = BitSet(0)

    private var roomLoadTaskRunning = false
    private var roomLoadSceneRunning = false

    private var paused = false

    fun withPlayer(player: Player) = withPlayer(player.key)
    fun withPlayer(name: String) = withPlayer(Player[name])
    fun withPlayer(key: ComponentKey) {
        if (key.componentIndex > NULL_COMPONENT_INDEX)
            players.set(key.componentIndex)
            val player = Player[key]
            player.addToGroup(ROOM_PAUSE_GROUP)
    }

    override fun load() {
        if (roomLoadTaskRunning) return

        roomLoadTaskRunning = true
        disposeAllCurrentRooms()
        currentRooms[this.index] = true

        // if there is a load scene, run it
        if (loadScene.exists) {
            if (loadInParallel) {
                // start load in new thread then start scene to run in parallel
                println("startParallelTask")
                startParallelTask("Room Load Task", { super.load() }) {
                    if (stopLoadSceneWhenLoadFinished) {
                        Scene.stopScene(loadScene.refIndex)
                        roomLoadSceneRunning = false
                    }
                    roomLoadTaskRunning = false
                }
                if (loadScene.exists) {
                    roomLoadSceneRunning = true
                    Scene.runScene(loadScene) { _, result ->
                        if (result == OperationResult.SUCCESS && activateAfterLoadScene)
                            Room.activate(this)
                        roomLoadSceneRunning = false
                    }
                }
            } else {
                // load first then show scene
                super.load()
                roomLoadTaskRunning = false
                if (loadScene.exists) {
                    roomLoadSceneRunning = true
                    Scene.runScene(loadScene) { _, result ->
                        if (result == OperationResult.SUCCESS && activateAfterLoadScene)
                            Room.activate(this)
                        roomLoadSceneRunning = false
                    }
                }
            }
        } else {
            super.load()
            roomLoadSceneRunning = false
        }
    }

    override fun activate() {
        // postpone activation processing if needed
        if (roomLoadTaskRunning || roomLoadSceneRunning) {
            println("RoomActivationPostpone")
            startParallelTask("RoomActivationPostpone", {
                while (roomLoadTaskRunning || roomLoadSceneRunning)
                    sleepCurrentThread(20)
            }) { activation() }
        } else // or just activate
            activation()
    }

    internal fun pause() {
        if (!active || paused)
            return

        println("pause room")
        // pause room pause group
        Pausing.pause(ROOM_PAUSE_GROUP)
        paused = true

        // play pause task if defined

    }

    internal fun resume() {
        if (!active || !paused)
            return

        println("resume room")
        Pausing.resume(ROOM_PAUSE_GROUP)
        paused = false
    }

    private fun activation() {
        if (activationScene.exists)
            Scene.runScene(this.activationScene) { _, _ -> super.activate() }
        else
            super.activate()

        currentRooms[this.index] = true
    }

    override fun deactivate() {
        super.deactivate()

        if (deactivationScene.exists)
            Scene.runScene(this.deactivationScene) { _, _ ->
                if (disposeAfterDeactivation)
                    Room.dispose(this)
            }
        else if (disposeAfterDeactivation)
            Room.dispose(this)
    }

    override fun dispose() {
        currentRooms[this.index] = false
        if (disposeScene.exists)
            Scene.runScene(this.disposeScene) { _, _ -> super.dispose() }
        else
            super.dispose()
    }


    @ThreadLocal
    companion object : ComponentSubTypeBuilder<Composite, Room>(Composite,"Room") {
        override fun create() = Room()

        const val ATTR_OBJECT_ID = "id"
        const val ATTR_OBJECT_TYPE = "type"
        const val ATTR_OBJECT_NAME = "name"
        const val ATTR_OBJECT_BOUNDS = "bounds"
        const val ATTR_OBJECT_ROTATION = "rotation"
        const val ATTR_OBJECT_VISIBLE = "visible"
        const val ATTR_OBJECT_GROUPS = "groups"

        @JvmField val ROOM_PAUSE_GROUP = COMPONENT_GROUP_ASPECT.createAspect("ROOM_PAUSE_GROUP")

        @JvmField var loadInParallel = false
        @JvmField var stopLoadSceneWhenLoadFinished = true
        @JvmField var autoDisposeCurrentRooms = true
        @JvmField var autoActivateLoadedRoom = true
        @JvmField var activateAfterLoadScene = true
        @JvmField var disposeAfterDeactivation = true

        private val currentRooms = BitSet()
        private fun disposeAllCurrentRooms() {
            if (autoDisposeCurrentRooms) {
                val iter = IndexIterator(currentRooms)
                while (iter.hasNext())
                    Room.dispose(iter.next())
            }
        }

        private val pauseUpdate = Engine.timer.createUpdateScheduler(20f)
        private val updatePause: () -> Unit = exit@ {

            if (!pauseUpdate.needsUpdate())
                return@exit

            var i = currentRooms.nextIndex(0)
            while (i > NULL_COMPONENT_INDEX) {
                val room = Room[i]
                i = currentRooms.nextIndex(i + 1)

                if (!room.inputDevice.buttonTyped(room.buttonPause))
                    continue

                if (room.paused) room.resume()
                else room.pause()
            }
        }

        init {
            Engine.registerListener(UPDATE_EVENT_TYPE, updatePause)
        }
    }
}




