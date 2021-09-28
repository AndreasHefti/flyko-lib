package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSingleType
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.collection.DynArray
import com.inari.util.geom.Rectangle
import kotlin.jvm.JvmField

class Area private constructor() : GenericComposite() {

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Rectangle = Rectangle()

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Area>(Composite, Area::class) {
        init { CompositeSystem.compositeBuilderMapping[Area::class.simpleName!!] = this }
        override fun createEmpty() = Area()
    }
}