package com.inari.firefly.physics.animation

import com.inari.firefly.DO_NOTHING
import com.inari.firefly.FFContext
import com.inari.firefly.NULL_CALL
import com.inari.firefly.control.Controller
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.core.component.CompId
import com.inari.firefly.core.component.ComponentDSL
import com.inari.firefly.core.component.ComponentType
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.game.world.SimpleCameraController
import com.inari.util.Call
import com.inari.util.collection.DynArray
import com.inari.util.geom.Easing
import com.inari.util.geom.GeomUtils
import kotlin.jvm.JvmField
import kotlin.reflect.KMutableProperty0

typealias PropertyAccessor<T> = KMutableProperty0<T>
typealias PropertyRefResolver<T> = (CompId) -> PropertyAccessor<T>

@ComponentDSL
class AnimatedObjectData<T>() {

    @JvmField internal var animationRef = -1

    @JvmField val animation = ComponentRefResolver(Animation) { animationRef = it }
    @JvmField var currentTime = 0f
    @JvmField var looping = false
    @JvmField var inverseOnLoop = false
    @JvmField var resetOnFinish = true
    @JvmField var inversed = false
    @JvmField var callback: Call = DO_NOTHING
    @JvmField var animatedProperty: PropertyRefResolver<T> = { _ -> throw IllegalStateException("NULL_FUNCTION") }

    lateinit var adapter: KMutableProperty0<T>
        protected set
    internal fun init(compId: CompId) { adapter = animatedProperty(compId) }

    fun setProperty(v: T)  { adapter.set(v) }
    fun getProperty(): T = adapter.get()

}

abstract class Animation protected constructor() : SystemComponent(Animation::class.simpleName!!) {

    @JvmField var duration = 0

    fun update() = update(FFContext.timer.timeElapsed.toFloat() / duration)
    abstract fun register(data: AnimatedObjectData<*>)
    abstract fun dispose(data: AnimatedObjectData<*>)
    abstract fun update(timeStep: Float)

    companion object : SystemComponentType<Animation>(Animation::class)
}

@Suppress("UNCHECKED_CAST")
abstract class FloatAnimation protected constructor() : Animation() {

     @JvmField var startValue = 0f
     @JvmField var endValue = 0f

     private val animatedData = DynArray.of<AnimatedObjectData<Float>>()

     override fun register(data: AnimatedObjectData<*>) {
         animatedData.add(data as AnimatedObjectData<Float>)
     }

     override fun dispose(data: AnimatedObjectData<*>) {
         animatedData.remove(data as AnimatedObjectData<Float>)
     }

     override fun update(timeStep: Float) {
         if (animatedData.isEmpty)
             return

         val i = animatedData.iterator()
         while (i.hasNext()) update(timeStep, i.next())
     }
     abstract fun update(timeStep: Float, data: AnimatedObjectData<Float>)
}

class EasedValueAnimation private constructor() : FloatAnimation() {

    @JvmField var easing: Easing.EasingFunctions.EasingFunction = Easing.Type.LINEAR

    override fun update(timeStep: Float, data: AnimatedObjectData<Float>) {
        data.currentTime += timeStep
        if (data.currentTime >= 1.0f) {
            data.currentTime = 0.0f
            // animation step finished
            if (data.looping) {
                if (data.inverseOnLoop)
                    data.inversed = !data.inversed
            } else {
                // animation finished
                if (data.resetOnFinish)
                    data.adapter.set(startValue)
                dispose(data)
                data.callback()
                return
            }
        }

        // animate
        val newVal = if (data.inversed)
            GeomUtils.lerp(endValue, startValue, easing(data.currentTime))
        else
            GeomUtils.lerp(startValue, endValue, easing(data.currentTime))
        data.adapter.set(newVal)
    }

    override fun componentType() = Animation.Companion
    companion object : SystemComponentSubType<Animation, EasedValueAnimation>(Animation, EasedValueAnimation::class) {
        override fun createEmpty() = EasedValueAnimation()
    }

}
