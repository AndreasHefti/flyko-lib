package com.inari.firefly.physics.animation

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField

class EAnimation private constructor() : EntityComponent(EAnimation) {

    @JvmField internal val animations = DynArray.of<AnimatedData>(2, 5)

    fun <AD : AnimatedData> withAnimation(builder: AnimatedDataBuilder<AD>, configure: AD.() -> Unit) {
        val result = builder.create()
        result.also(configure)
        animations + result
    }

    override fun activate() {
        super.activate()
        animations.forEach { it.activate(entityIndex) }
    }

    override fun reset() {
        animations.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EAnimation>("EAnimation") {
        override fun create() = EAnimation()
    }
}