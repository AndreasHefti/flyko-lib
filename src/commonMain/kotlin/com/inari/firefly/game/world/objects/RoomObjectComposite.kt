package com.inari.firefly.game.world.objects

import com.inari.firefly.composite.Composite
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.graphics.view.Layer
import com.inari.firefly.graphics.view.View
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField


abstract class RoomObjectComposite : Composite() {

    protected var viewRef = -1
    protected var layerRef = -1

    @JvmField internal val attributes = mutableMapOf<String, String>()

    @JvmField var view = ComponentRefResolver(View) { index -> viewRef = index }
    @JvmField var layer = ComponentRefResolver(Layer) { index -> layerRef = index }
    @JvmField val position = Vector2f()


    fun setAttribute(name: String, value: String) { attributes[name] = value }
    fun getAttribute(name: String): String? = attributes[name]
    fun getAttributeFloat(name: String): Float? = attributes[name]?.toFloat()

}