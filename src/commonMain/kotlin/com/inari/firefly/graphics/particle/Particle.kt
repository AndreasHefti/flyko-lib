package com.inari.firefly.graphics.particle

import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.api.BindingIndex
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.core.api.TransformData
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.util.ZERO_FLOAT
import kotlin.jvm.JvmField

@ComponentDSL
abstract class Particle protected constructor() : TransformData() {

//    @JvmField
//    internal val transformData = TransformDataImpl()

//    val position: Vector2f
//        get() = transformData.position
//    val pivot: Vector2f
//        get() = transformData.pivot
//    val scale: Vector2f
//        get() = transformData.scale
//    var rotation: Float
//        get() = transformData.rotation
//        set(value) { transformData.rotation = value }
    @JvmField var xVelocity: Float  = ZERO_FLOAT
    @JvmField var yVelocity: Float  = ZERO_FLOAT
    @JvmField var mass: Float  = 1f

    interface ParticleBuilder<P : Particle> {
        fun createEmpty(): P
    }
}

class SpriteParticle: Particle() {

    @JvmField val renderData = SpriteRenderable()

    var spriteIndex: BindingIndex
        get() = renderData.spriteIndex
        set(value) { renderData.spriteIndex = value }
    val tintColor
        get() = renderData.tintColor
    var blendMode
        get() = renderData.blendMode
        set(value) { renderData.blendMode = value }
    @JvmField val spriteRef = CReference(Sprite)

    companion object : ParticleBuilder<SpriteParticle> {
        override fun createEmpty(): SpriteParticle =
            SpriteParticle()

        val of: (SpriteParticle.() -> Unit) -> SpriteParticle = { configure ->
            val instance = SpriteParticle()
            instance.also(configure)
            instance
        }
    }
}