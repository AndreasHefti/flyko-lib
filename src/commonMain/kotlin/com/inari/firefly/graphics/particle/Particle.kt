package com.inari.firefly.graphics.particle

import com.inari.firefly.core.Asset
import com.inari.firefly.core.CReference
import com.inari.firefly.core.ComponentDSL
import com.inari.firefly.core.api.BlendMode
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.core.api.TransformDataImpl
import com.inari.firefly.graphics.sprite.Sprite
import com.inari.util.ZERO_FLOAT
import com.inari.util.geom.Vector2f
import com.inari.util.geom.Vector4f
import kotlin.jvm.JvmField

@ComponentDSL
abstract class Particle protected constructor() {

    @JvmField
    internal val transformData = TransformDataImpl()

    val position: Vector2f
        get() = transformData.position
    val pivot: Vector2f
        get() = transformData.pivot
    val scale: Vector2f
        get() = transformData.scale
    var rotation: Float
        get() = transformData.rotation
        set(value) { transformData.rotation = value }
    @JvmField var xVelocity: Float  = ZERO_FLOAT
    @JvmField var yVelocity: Float  = ZERO_FLOAT
    @JvmField var mass: Float  = 1f

    interface ParticleBuilder<P : Particle> {
        fun createEmpty(): P
    }
}

class SpriteParticle(): Particle(), SpriteRenderable {

    val spriteRef = CReference(Sprite)
    override val spriteIndex: Int
        get() = Asset.resolveAssetIndex(spriteRef.targetKey)
    override val tintColor = Vector4f()
    override val blendMode = BlendMode.NORMAL_ALPHA

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