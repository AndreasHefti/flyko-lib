package com.inari.firefly.physics.animation

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.util.collection.BitSet
import com.inari.util.collection.DynArray
import kotlin.jvm.JvmField


class EAnimation : EntityComponent(EAnimation::class.simpleName!!) {

    @JvmField internal val animations = DynArray.of<AnimatedObjectData<*>>(2, 5)

    fun <A> withAnimated(builder: AnimatedObjectData<A>.() -> Unit) {
        val result = AnimatedObjectData<A>()
        result.also(builder)
        animations + result
    }

    override fun reset() {
        animations.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EAnimation>(EAnimation::class) {
        override fun createEmpty() = EAnimation()
    }
}