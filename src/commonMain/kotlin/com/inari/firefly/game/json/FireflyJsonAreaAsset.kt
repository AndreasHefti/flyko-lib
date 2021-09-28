package com.inari.firefly.game.json

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.world.Area
import com.inari.firefly.game.world.Room
import com.inari.firefly.game.world.WorldSystem
import com.inari.util.StringUtils
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

            if (areaData.onActivationTasks != null)
                areaData.onActivationTasks.split(StringUtils.KEY_VALUE_SEPARATOR).forEach { taskName ->
                    withActivationTask(taskName)
                }
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

                if (roomData.onActivationTasks != null)
                    roomData.onActivationTasks.split(StringUtils.KEY_VALUE_SEPARATOR).forEach { taskName ->
                        withActivationTask(taskName)
                    }
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