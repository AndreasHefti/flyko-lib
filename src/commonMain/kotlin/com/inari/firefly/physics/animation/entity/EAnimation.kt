package com.inari.firefly.physics.animation.entity

import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.physics.animation.Animation
import com.inari.util.collection.BitSet
import kotlin.jvm.JvmField


class EAnimation : EntityComponent(EAnimation::class.simpleName!!) {

    @JvmField internal val animations: BitSet = BitSet()

    val animation = ComponentRefResolver(Animation) { index->
        if (initialized)
            throw IllegalStateException("EAnimation instance is already created")
        animations.set(index)
    }

    fun <A : Animation> animation(builder: SystemComponentSubType<Animation, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EAnimation instance is already created")
        val id = builder.build(configure)
        animations.set(id.index)
        return id
    }

    fun <A : Animation> activeAnimation(builder: SystemComponentSubType<Animation, A>, configure: (A.() -> Unit)): CompId {
        if (initialized)
            throw IllegalStateException("EAnimation instance is already created")
        val id = builder.buildAndActivate(configure)
        animations.set(id.index)
        return id
    }

    override fun reset() {
        animations.clear()
    }

    override fun componentType() = Companion
    companion object : EntityComponentType<EAnimation>(EAnimation::class) {
        override fun createEmpty() = EAnimation()
    }
}