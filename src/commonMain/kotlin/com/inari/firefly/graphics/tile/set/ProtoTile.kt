package com.inari.firefly.graphics.tile.set

import com.inari.firefly.*
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.Aspects
import com.inari.util.geom.BitMask
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

@ComponentDSL
class ProtoTile internal constructor() {

    @JvmField internal var spriteData: ProtoSprite? = null
    @JvmField internal var animationData: TileAnimation? = null
    @JvmField internal var entityRef = -1

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
    @JvmField val animation: (TileAnimation.() -> Unit) -> Unit = { configure ->
        val animationData = TileAnimation.of {}
        animationData.also(configure)
        this.animationData = animationData
    }
    @JvmField val sprite: (ProtoSprite.() -> Unit) -> Unit = { configure ->
        val sprite = ProtoSprite.of {}
        sprite.also(configure)
        this.spriteData = sprite
    }

    val hasContactComp: Boolean
        get() = contactType !== UNDEFINED_CONTACT_TYPE ||
                material !== UNDEFINED_MATERIAL

    companion object {
        val of: (ProtoTile.() -> Unit) -> ProtoTile = { configure ->
            val comp = ProtoTile()
            comp.also(configure)
            comp
        }
    }
}