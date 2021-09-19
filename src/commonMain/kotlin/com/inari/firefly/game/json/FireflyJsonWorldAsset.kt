package com.inari.firefly.game.json

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.NO_NAME
import com.inari.firefly.asset.Asset
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.game.world.World
import com.inari.util.StringUtils
import com.inari.util.Supplier
import kotlin.jvm.JvmField

class FireflyJsonWorldAsset private constructor() : Asset() {

    private var resource: Supplier<WorldJson> = { throw RuntimeException() }
    private var worldId = NO_COMP_ID

    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { FFContext.resourceService.loadJSONResource(value, WorldJson::class, encryptionKey) }
        }
    fun withWorldData(areaData: WorldJson) {
        resource = { areaData }
    }


    override fun load() {
        val worldData = resource.invoke()

        // create  the world component
        worldId = World.build {
            name = worldData.name
            orientationType = worldData.orientationType
            orientation(worldData.orientation)
            attributes.putAll(worldData.attributes)

            if (worldData.onActivationTasks != null)
                worldData.onActivationTasks.split(StringUtils.KEY_VALUE_SEPARATOR).forEach { taskName ->
                    withActivationTask(taskName)
                }

            // create area assets to load areas from json resources
            worldData.areasData.forEach { areaData ->
                withAsset(
                    FireflyJsonAreaAsset.build {
                        name = areaData.name
                        resourceName = areaData.resource
                    }
                )
            }
        }
    }

    override fun unload() {
        if (worldId == NO_COMP_ID)
            return

        // delete all area assets. This implicates also dispose them first if active
        FFContext.deleteAllQuietly(Asset,  FFContext[World, worldId].assetRefs)
        // delete the world
        FFContext.delete(worldId)
        // cleanup
        worldId = NO_COMP_ID
    }

    override fun instanceId(index: Int): Int = worldId.instanceId

    companion object : SystemComponentSubType<Asset, FireflyJsonWorldAsset>(Asset, FireflyJsonWorldAsset::class) {
        override fun createEmpty() = FireflyJsonWorldAsset()
    }
}