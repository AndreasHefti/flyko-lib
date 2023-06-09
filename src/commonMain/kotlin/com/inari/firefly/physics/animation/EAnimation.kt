package com.inari.firefly.physics.animation

import com.inari.firefly.core.ComponentBuilder
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentSystem
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import com.inari.util.collection.IndexIterator

class EAnimation private constructor() : EntityComponent(EAnimation) {

    private val animationRefs = BitSet()
    private var registered = false

    fun <A : Animation> withAnimation(builder: ComponentBuilder<A>, configure: A.() -> Unit) {
        val result = builder.build(configure)
        animationRefs.set(result.componentIndex)
    }

    fun withAnimation(animation: Animation) =
        animationRefs.set(animation.index)


    override fun activate() {
        super.activate()
        if (!registered) {
            val iter = IndexIterator(animationRefs)
            while (iter.hasNext())
                Animation[iter.nextInt()].register(entityIndex)
        }
    }

    override fun deactivate() {
        val iter = IndexIterator(animationRefs)
        while (iter.hasNext())
            Animation[iter.nextInt()].finish()
        super.deactivate()
    }

    override fun reset() {
        val iter = IndexIterator(animationRefs)
        while (iter.hasNext())
            Animation[iter.nextInt()].dispose(entityIndex)
        animationRefs.clear()
        registered = false
    }

    override val componentType = Companion
    companion object : EntityComponentSystem<EAnimation>("EAnimation") {
        override fun allocateArray() = DynArray.of<EAnimation>()
        override fun create() = EAnimation()
    }
}