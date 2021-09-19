@file:Suppress("VARIABLE_IN_SINGLETON_WITHOUT_THREAD_LOCAL")

package com.inari.firefly.game.world

import com.inari.firefly.FFContext
import com.inari.firefly.NO_COMP_ID
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.FFSystem
import com.inari.util.collection.DynArray
import com.inari.util.collection.DynArrayRO

object WorldSystem : FFSystem {

    val worlds: DynArrayRO<World>
        get() = systemWorlds
    private val systemWorlds = DynArray.of<World>(5, 10)
    var activeWorld = NO_COMP_ID
        private set

    val areas: DynArrayRO<Area>
        get() = systemAreas
    private val systemAreas = DynArray.of<Area>(5, 10)
    var activeArea = NO_COMP_ID
        private set

    val rooms: DynArrayRO<Room>
        get() = systemRooms
    private val systemRooms = DynArray.of<Room>(5, 10)
    var activeRoom = NO_COMP_ID
        private set

    val loadWorld = ComponentRefResolver(World) { worldId ->
        val world = FFContext[World, worldId]
        CompositeSystem.loadComposite(world)
        systemWorlds.add(world)
        WorldEvent.send(WorldEventType.WORLD_LOADED, world.componentId)
    }

    val activateWorld = ComponentRefResolver(World) { worldId ->
        if (activeWorld != NO_COMP_ID)
            deactivateWorld()

        val world = FFContext[World, worldId]
        CompositeSystem.activateComposite(world)
        activeWorld = world.componentId
        WorldEvent.send(WorldEventType.WORLD_ACTIVATED, world.componentId)
    }

    fun deactivateWorld() {
        if (activeWorld == NO_COMP_ID)
            return

        val world = FFContext[World, activeWorld]
        CompositeSystem.deactivateComposite(world)
        activeWorld = NO_COMP_ID
        WorldEvent.send(WorldEventType.WORLD_DEACTIVATED, world.componentId)
    }

    val disposeWorld = ComponentRefResolver(World) { worldId ->
        if (activeWorld != NO_COMP_ID && activeWorld.instanceId == worldId)
            deactivateWorld()

        val world = FFContext[World, worldId]
        CompositeSystem.disposeComposite(world)
        systemWorlds.remove(world)
        WorldEvent.send(WorldEventType.WORLD_DISPOSED, world.componentId)
    }

    val loadArea = ComponentRefResolver(Area) { areaId ->
        val area = FFContext[Area, areaId]
        CompositeSystem.loadComposite(area)
        systemAreas.add(area)
        WorldEvent.send(WorldEventType.AREA_LOADED, area.componentId)
    }

    val activateArea = ComponentRefResolver(Area) { areaId ->
        if (activeArea != NO_COMP_ID)
            deactivateArea()

        val area = FFContext[Area, areaId]
        CompositeSystem.activateComposite(area)
        activeArea = area.componentId
        WorldEvent.send(WorldEventType.AREA_ACTIVATED, area.componentId)
    }

    fun deactivateArea() {
        if (activeArea == NO_COMP_ID)
            return

        val area = FFContext[Area, activeArea]
        CompositeSystem.deactivateComposite(area)
        activeArea = NO_COMP_ID
        WorldEvent.send(WorldEventType.AREA_DEACTIVATED, area.componentId)
    }

    val disposeArea = ComponentRefResolver(Area) { areaId ->
        if (activeArea != NO_COMP_ID && activeArea.instanceId == areaId)
            deactivateArea()

        val area = FFContext[Area, areaId]
        CompositeSystem.disposeComposite(area)
        systemAreas.remove(area)
        WorldEvent.send(WorldEventType.AREA_DISPOSED, area.componentId)
    }

    val loadRoom = ComponentRefResolver(Room) { roomId ->
        val room = FFContext[Room, roomId]
        CompositeSystem.loadComposite(room)
        systemRooms.add(room)
        WorldEvent.send(WorldEventType.ROOM_LOADED, room.componentId)
    }

    val activateRoom = ComponentRefResolver(Room) { roomId ->
        if (activeRoom != NO_COMP_ID)
            deactivateRoom()

        val room = FFContext[Room, roomId]
        CompositeSystem.activateComposite(room)
        activeRoom = room.componentId
        WorldEvent.send(WorldEventType.ROOM_ACTIVATED, room.componentId)
    }

    fun deactivateRoom() {
        if (activeRoom == NO_COMP_ID)
            return

        val room = FFContext[Room, activeRoom]
        CompositeSystem.deactivateComposite(room)
        activeRoom = NO_COMP_ID
        WorldEvent.send(WorldEventType.ROOM_DEACTIVATED, room.componentId)
    }

    val disposeRoom = ComponentRefResolver(Room) { roomId ->
        if (activeRoom != NO_COMP_ID && activeRoom.instanceId == roomId)
            deactivateRoom()

        val room = FFContext[Room, roomId]
        CompositeSystem.disposeComposite(room)
        systemRooms.remove(room)
        WorldEvent.send(WorldEventType.ROOM_DISPOSED, room.componentId)
    }

    override fun clearSystem() {
        deactivateRoom()
        deactivateArea()
        deactivateWorld()

        systemRooms.forEach { disposeRoom(it) }
        systemRooms.clear()
        systemAreas.forEach { disposeArea(it) }
        systemAreas.clear()
        systemWorlds.forEach { disposeWorld(it) }
        systemWorlds.clear()
    }
}