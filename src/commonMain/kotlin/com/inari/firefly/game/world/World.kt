package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField

class World : GenericComposite() {

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Rectangle = Rectangle()

    override fun componentType() = Room
    companion object : SystemComponentSubType<Composite, World>(Composite, World::class) {
        init { CompositeSystem.compositeBuilderMapping[World::class.simpleName!!] = this }
        override fun createEmpty() = World()
    }
}