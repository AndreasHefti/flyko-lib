package com.inari.firefly.graphics.sprite

import com.inari.firefly.NO_NAME
import com.inari.firefly.core.component.IndexedInstantiable
import com.inari.firefly.core.component.ComponentDSL
import com.inari.util.geom.Vector4i
import kotlin.jvm.JvmField

@ComponentDSL
class ProtoSprite internal constructor() : IndexedInstantiable {

    @JvmField internal var instId = -1
    override val instanceId: Int get() = instId

    @JvmField var name: String = NO_NAME
    @JvmField val textureBounds: Vector4i = Vector4i()
    @JvmField var hFlip: Boolean = false
    @JvmField var vFlip: Boolean = false

}