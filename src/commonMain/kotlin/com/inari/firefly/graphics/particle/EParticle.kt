package com.inari.firefly.graphics.particle

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.graphics.view.EntityRenderer
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField


class EParticle private constructor() : EntityComponent(EParticle) {

    @JvmField var renderer: EntityRenderer = SpriteParticleRenderer
    @JvmField val particles: DynArray<Particle> = DynArray.of()

    fun <P : Particle> particle(builder: Particle.ParticleBuilder<P>, configure: (P.() -> Unit)) {
        val particle = builder.createEmpty()
        particle.also(configure)
        particles.add(particle)
    }

    override fun reset() {
        renderer = SpriteParticleRenderer
        particles.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EParticle>("EParticle") {
        override fun allocateArray() = DynArray.of<EParticle>()
        override fun create() = EParticle()
    }
}