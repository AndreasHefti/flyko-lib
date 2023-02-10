package com.inari.firefly.game.room

import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentKey
import com.inari.firefly.core.ComponentSubTypeBuilder
import com.inari.firefly.core.Composite
import kotlin.jvm.JvmField

class Room private constructor() : Composite(Room) {

    @JvmField val tileMap = CReference(TileMap)

    fun withTileMap(configure: (TileMap.() -> Unit)): ComponentKey {
        val key = TileMap.build(configure)
        tileMap(key)
        return key
    }

    override fun load() {
        super.load()
        if (tileMap.exists)
            TileMap.load(tileMap.targetKey)
    }

    override fun activate() {
        super.activate()
        if (tileMap.exists)
            TileMap.activate(tileMap.targetKey)
    }

    override fun deactivate() {
        if (tileMap.exists)
            TileMap.deactivate(tileMap.targetKey)
        super.deactivate()
    }

    override fun dispose() {
        if (tileMap.exists)
            TileMap.dispose(tileMap.targetKey)
        super.dispose()
    }

    override fun delete() {
        if (tileMap.exists)
             TileMap.delete(tileMap.targetKey)
        super.delete()
    }

    var paused = false
        internal set

    companion object : ComponentSubTypeBuilder<Composite, Room>(Composite,"Room") {
        override fun create() = Room()
    }
}
