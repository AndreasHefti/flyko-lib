package com.inari.firefly.graphics.particle

import com.inari.firefly.BlendMode
import com.inari.firefly.FFContext
import com.inari.firefly.asset.AssetInstanceRefResolver
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.api.SpriteRenderable
import com.inari.firefly.graphics.effect.ShaderEffectAsset
import com.inari.util.graphics.MutableColor
import kotlin.jvm.JvmField

class SpriteParticle : Particle() {

    @JvmField internal val spriteRenderable = SpriteRenderable()
    @JvmField internal val spriteRef: Int = -1

    val sprite = AssetInstanceRefResolver(
            { index -> spriteRenderable.spriteId = index },
            { spriteRenderable.spriteId })
    val effect = AssetInstanceRefResolver(
        { index -> spriteRenderable.effectInstanceRef = FFContext[ShaderEffectAsset, index].instanceId },
        { spriteRenderable.effectInstanceRef })
    var blend: BlendMode
        get() = spriteRenderable.blendMode
        set(value) { spriteRenderable.blendMode = value }
    var tint: MutableColor
        get() = spriteRenderable.tintColor
        set(value) { spriteRenderable.tintColor(value) }

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