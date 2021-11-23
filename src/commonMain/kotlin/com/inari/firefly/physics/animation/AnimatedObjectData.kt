package com.inari.firefly.physics.animation

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFContext
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.*
import com.inari.firefly.core.system.SystemComponentBuilder
import com.inari.util.Call
import kotlin.jvm.JvmField
import kotlin.reflect.KMutableProperty0

@ComponentDSL
class AnimatedObjectData<T> {

    @JvmField internal var animationRef = -1

    @JvmField var normalizedTime = 0f
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var resetOnFinish = true
    @JvmField var inversed = false
    @JvmField var callback: Call = DO_NOTHING
    @JvmField var animatedProperty: PropertyRefResolver<T> = NULL_PROPERTY_REF_RESOLVER()
    @JvmField var adapter: KMutableProperty0<T> = this::NULL_ADAPTER

    var NULL_ADAPTER: T
        get() = throw IllegalStateException("Adapter not set")
        set(value) = throw IllegalStateException("Adapter not set")

    internal inline fun init(compId: CompId) { adapter = animatedProperty(compId) }

    @JvmField val applyToAnimation = ComponentRefResolver(Animation) { animationRef = it }
    fun <A : Animation> applyToNewAnimation(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = cBuilder.build(configure)
        animationRef = result.instanceId
        return result
    }
    fun <A : Animation> applyToNewActiveAnimation(cBuilder: SystemComponentBuilder<A>, configure: (A.() -> Unit)): CompId {
        val result = applyToNewAnimation(cBuilder, configure)
        FFContext.activate(result)
        return result
    }

    inline fun setProperty(v: T)  { adapter.set(v) }
    inline fun getProperty(): T = adapter.get()
}