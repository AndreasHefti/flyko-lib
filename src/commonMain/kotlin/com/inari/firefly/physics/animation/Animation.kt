package com.inari.firefly.physics.animation

import com.inari.firefly.NULL_CALL
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.util.Call

abstract class Animation protected constructor() : SystemComponent(Animation::class.simpleName!!) {

    var looping: Boolean = false
    var resetOnFinish: Boolean = true
    var callback: Call = NULL_CALL

    abstract fun update()
    abstract fun reset()

    override fun componentType() = Companion
    companion object : SystemComponentType<Animation>(Animation::class)
}

interface FloatAnimation { val value: Float }
interface IntAnimation { val value: Int }
interface ValueAnimation<out T> { val value: T }