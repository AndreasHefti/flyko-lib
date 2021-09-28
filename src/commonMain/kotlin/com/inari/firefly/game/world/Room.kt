package com.inari.firefly.game.world

import com.inari.firefly.composite.Composite
import com.inari.firefly.composite.CompositeSystem
import com.inari.firefly.composite.GenericComposite
import com.inari.firefly.control.scene.Scene
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.util.geom.Rectangle
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField

class Room private constructor(): GenericComposite() {

    internal var activationSceneRef = -1
    @JvmField val activationScene = ComponentRefResolver(Scene) { activationSceneRef = it }

    internal var deactivationSceneRef = -1
    @JvmField val deactivationScene = ComponentRefResolver(Scene) { deactivationSceneRef = it }

    @JvmField var roomOrientationType: WorldOrientationType = WorldOrientationType.PIXELS
    @JvmField val roomOrientation: Rectangle = Rectangle()
    @JvmField val tileDimension: Vector2i = Vector2i()

    @JvmField var areaOrientationType: WorldOrientationType = WorldOrientationType.SECTION
    @JvmField val areaOrientation: Rectangle = Rectangle()

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Composite, Room>(Composite, Room::class) {
        init { CompositeSystem.compositeBuilderMapping[Room::class.simpleName!!] = this }
        override fun createEmpty() = Room()
    }
}