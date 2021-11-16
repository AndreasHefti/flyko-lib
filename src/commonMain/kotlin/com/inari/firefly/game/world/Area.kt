package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

class Area private constructor() : GenericComposite() {

    @JvmField var orientationType: WorldOrientationType = WorldOrientationType.COUNT
    @JvmField val orientation: Vector4i = Vector4i()

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Area>(Composite, Area::class) {
        init { CompositeSystem.compositeBuilderMapping[Area::class.simpleName!!] = this }
        override fun createEmpty() = Area()
    }
}