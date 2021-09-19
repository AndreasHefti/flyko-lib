package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField

class Room : GenericComposite() {

    var parentRef = -1
        internal set
    val withParent = ComponentRefResolver(Area) { index -> parentRef + index }

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Rectangle = Rectangle()

    companion object : SystemComponentSubType<Composite, Room>(Composite, Room::class) {
        init { CompositeSystem.compositeBuilderMapping[Room::class.simpleName!!] = this }
        override fun createEmpty() = Room()
    }
}