package com.inari.firefly.graphics.particle

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.ArrayAccessor
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.graphics.rendering.Renderer
import com.inari.firefly.graphics.rendering.SpriteParticleRenderer
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class EParticle private constructor() : EntityComponent(EParticle::class.simpleName!!) {

    @JvmField internal var rendererRef = SpriteParticleRenderer.instance.index
    internal val int_particle: DynArray<Particle> = DynArray.of()

    var renderer = ComponentRefResolver(Renderer) { index-> rendererRef = index }
    var particle = ArrayAccessor(int_particle)
    fun <P : Particle> particle(builder: Particle.ParticleBuilder<P>, configure: (P.() -> Unit)) {
        val particle = builder.createEmpty()
        particle.also(configure)
        this.particle.add(particle)
    }

    override fun reset() {
        rendererRef = -1
        int_particle.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EParticle>(EParticle::class) {
        override fun createEmpty() = EParticle()
    }
}