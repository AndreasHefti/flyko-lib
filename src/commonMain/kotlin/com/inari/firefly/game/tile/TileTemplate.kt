package com.inari.firefly.game.tile

import com.inari.firefly.*
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.geom.BitMask
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

@ComponentDSL
class TileTemplate internal constructor() {

    @JvmField internal val protoSprite = ProtoSprite()
    @JvmField internal var animationData: TileAnimation? = null

    @JvmField var name: String = NO_NAME
    @Suppress("SetterBackingFieldAssignment")
    var aspects: Aspects = TILE_ASPECT_GROUP.createAspects()
        set(value) {
            field.clear()
            field + value
        }
    @JvmField var material: Aspect = UNDEFINED_MATERIAL
    @JvmField var contactType: Aspect = UNDEFINED_CONTACT_TYPE
    @JvmField var contactMask: BitMask? = null
    @JvmField var tintColor: MutableColor? = null
    @JvmField var blendMode: BlendMode? = null
    @JvmField val withAnimation: (TileAnimation.() -> Unit) -> Unit = { configure ->
        animationData = TileAnimation()
        animationData!!.also(configure)
    }
    @JvmField val withSprite: (ProtoSprite.() -> Unit) -> Unit = { configure ->
        protoSprite.also(configure)
    }

    val hasContactComp: Boolean
        get() = contactType !== UNDEFINED_CONTACT_TYPE ||
                material !== UNDEFINED_MATERIAL
}