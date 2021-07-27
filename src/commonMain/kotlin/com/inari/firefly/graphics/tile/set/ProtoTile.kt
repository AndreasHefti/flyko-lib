package com.inari.firefly.graphics.tile.set

import com.inari.firefly.BlendMode
import com.inari.firefly.UNDEFINED_CONTACT_TYPE
import com.inari.firefly.UNDEFINED_MATERIAL
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.graphics.sprite.ProtoSprite
import com.inari.firefly.graphics.tile.ETile.Companion.TILE_ASPECTS
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

    @Suppress("SetterBackingFieldAssignment")
    var aspects: Aspects = TILE_ASPECTS.createAspects()
        set(value) {
            field.clear()
            field + value
        }
    var material: Aspect = UNDEFINED_MATERIAL
    var contactType: Aspect = UNDEFINED_CONTACT_TYPE
    var contactMask: BitMask? = null
    var tintColor: MutableColor? = null
    var blendMode: BlendMode? = null
    val animation: (TileAnimation.() -> Unit) -> Unit = { configure ->
        val animationData = TileAnimation.of {}
        animationData.also(configure)
        this.animationData = animationData
    }
    val sprite: (ProtoSprite.() -> Unit) -> Unit = { configure ->
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