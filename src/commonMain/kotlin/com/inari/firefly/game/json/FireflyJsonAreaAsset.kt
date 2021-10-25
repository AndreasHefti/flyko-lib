package com.inari.firefly.game.json

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.world.Area
import com.inari.firefly.game.world.Room
import com.inari.util.Supplier
import kotlin.jvm.JvmField

class FireflyJsonAreaAsset private constructor() : Asset() {

    private var resource: Supplier<AreaJson> = { throw RuntimeException() }
    private var areaId = NO_COMP_ID

    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, AreaJson::class, encryptionKey) }
        }
    fun withAreaData(areaData: AreaJson) {
        resource = { areaData }
    }

    override fun load() {
        val areaData = resource.invoke()

        // create area
        areaId = Area.build {
            name = areaData.name
            orientationType = areaData.orientationType
            orientation(areaData.orientation)
            attributes.putAll(areaData.attributes)

            loadTasks = areaData.onLoadTasks
            activationTasks = areaData.onActivationTasks
            deactivationTasks = areaData.onDeactivationTasks
            disposeTasks = areaData.onDisposeTasks
        }

        // create rooms meta data
        areaData.roomsData.forEach { roomData ->
            val roomId = Room.build {
                name = roomData.name
                roomOrientationType = roomData.orientationType
                roomOrientation(roomData.orientation)
                areaOrientationType = roomData.areaOrientationType
                areaOrientation(roomData.areaOrientation)
                attributes.putAll(roomData.attributes)

                loadTasks = roomData.onLoadTasks
                activationTasks = roomData.onActivationTasks
                deactivationTasks = roomData.onDeactivationTasks
                disposeTasks = roomData.onDisposeTasks

                if (roomData.pauseTask != NO_NAME)
                    withPauseTask(roomData.pauseTask)
                if (roomData.resumeTask != NO_NAME)
                    withResumeTask(roomData.resumeTask)
                if (roomData.activationScene != NO_NAME)
                    withActivationScene(roomData.activationScene)
                if (roomData.deactivationScene != NO_NAME)
                    withDeactivationScene(roomData.deactivationScene)
            }
        }
    }

    override fun unload() {
        if (areaId == NO_COMP_ID)
            return

        // delete the area
        FFContext.delete(areaId)
        areaId = NO_COMP_ID
    }

    override fun instanceId(index: Int): Int = areaId.instanceId

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Asset, FireflyJsonAreaAsset>(Asset, FireflyJsonAreaAsset::class) {
        override fun createEmpty() = FireflyJsonAreaAsset()
    }
}