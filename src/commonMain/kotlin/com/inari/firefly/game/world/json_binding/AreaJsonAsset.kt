package com.inari.firefly.game.world.json_binding

import com.inari.firefly.core.*
import com.inari.firefly.game.world.Area
import com.inari.firefly.game.world.Room
import com.inari.util.Attributes
import com.inari.util.NO_NAME
import kotlin.jvm.JvmField

class AreaJsonAsset private constructor() : Asset(AreaJsonAsset) {

    private var resource: () -> AreaData = { throw RuntimeException() }
    var areaData: AreaData? = null
        internal set

    @JvmField var autoBuildAreaComponent = false
    @JvmField var encryptionKey: String? = null
    var resourceName: String = NO_NAME
        set(value) {
            field = value
            resource = { Engine.resourceService.loadJSONResource(value, AreaData::class, encryptionKey) }
        }
    fun withAreaData(areaData: AreaData) {
        resource = { areaData }
    }

    override fun load() {
        val areaData = resource()
        if (autoBuildAreaComponent)
            buildArea(areaData)
    }

    fun buildRoom(roomName: String) {
        findRoomDataByName(roomName, areaData ?: return)?.also { buildRoom(it) }
    }

    fun buildArea(areaDataName: String) {
        findAreaDataByName(areaDataName, areaData ?: return)?.also { buildArea(it) }
    }

    fun buildArea(data: AreaData, withChildAreas: Boolean = true, withRooms: Boolean = true): ComponentKey {
        return Area {
            name = data.name
            orientationType = data.orientationType
            orientation(data.orientation)
            attributes = Attributes(data.attributes)
            // TODO refactoring
//            onLoadTask(data.onLoadTasks)
//            onActivationTask(data.onActivationTasks)
//            onDeactivationTask(data.onDeactivationTasks)
//            onDisposeTask(data.onDisposeTasks)

//            if (withChildAreas && !data.areasData.isEmpty()) {
//                data.areasData.forEach {
//                    withChild(this@AreaJsonAsset.buildArea(it, withChildAreas, withRooms))
//                }
//            }
//
//            if (withRooms && !data.roomsData.isEmpty()) {
//                data.roomsData.forEach {
//                    withChild(this@AreaJsonAsset.buildRoom(it))
//                }
//            }
        }
    }

    fun buildRoom(data: RoomData): ComponentKey =
        Room {
            name = data.name
            //parentName = data.name
            areaOrientationType = data.areaOrientationType
            roomOrientationType = data.roomOrientationType
            roomOrientation(data.roomOrientation)
            areaOrientationType = data.areaOrientationType
            areaOrientation(data.areaOrientation)
            attributes = Attributes(data.attributes)
            // TODO refactoring
//            onLoadTask(data.onLoadTasks)
//            onActivationTask(data.onActivationTasks)
//            onDeactivationTask(data.onDeactivationTasks)
//            onDisposeTask(data.onDisposeTasks)
            pauseTask(data.pauseTask)
            resumeTask(data.resumeTask)
            activationScene(data.activationScene)
            deactivationScene(data.deactivationScene)
        }

    private fun findAreaDataByName(name: String, data: AreaData): AreaData? {
        if (data.name == name) return data
        return data.areasData.find { findAreaDataByName(name, it) != null }
    }

    private fun findRoomDataByName(name: String, data: AreaData): RoomData? {
        val room = data.roomsData.find { it.name == name }
        if (room != null) {
            return room
        }
        data.areasData.forEach {
            val r = findRoomDataByName(name, it)
            if (r != null)
                return r
        }
        return null
    }

    override fun dispose() {
        super.dispose()
        if (autoBuildAreaComponent)
            Area.delete(areaData!!.name)
        areaData = null
    }

    override fun assetIndex(at: Int) = -1

    companion object : ComponentSubTypeBuilder<Asset, AreaJsonAsset>(Asset,"AreaJsonAsset") {
        override fun create() = AreaJsonAsset()
    }
}