package com.inari.firefly.physics.animation

import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.util.collection.BitSet
import com.inari.util.collection.IndexIterator

class EAnimation private constructor() : EntityComponent(EAnimation) {

    private val dataRefs = BitSet()

    fun <AD : AnimatedData<AD>> withAnimation(builder: AnimatedDataBuilder<AD>, configure: AD.() -> Unit) {
        val result = builder.create()
        result.also(configure)
        dataRefs.set(AnimationSystem.animations.add(result))
    }

    override fun activate() {
        super.activate()
        val iter = IndexIterator(dataRefs)
        while (iter.hasNext())
            AnimationSystem.animations[iter.next()]?.init(entityIndex)
    }

    override fun deactivate() {
        val iter = IndexIterator(dataRefs)
        while (iter.hasNext())
            AnimationSystem.animations[iter.next()]?.active = false
        super.deactivate()
    }

    override fun reset() {
        val iter = IndexIterator(dataRefs)
        while (iter.hasNext())
            AnimationSystem.animations.remove(iter.next())
        dataRefs.clear()
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EAnimation>("EAnimation") {
        override fun create() = EAnimation()
    }
}