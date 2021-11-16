package com.inari.firefly.graphics.particle

import com.inari.firefly.core.api.TransformData
import com.inari.firefly.core.component.ComponentDSL
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

@ComponentDSL
abstract class Particle protected constructor() {

    @JvmField internal val transformData = TransformData()
    
    val position: Vector2f
        get() = transformData.position
    val pivot: Vector2f
        get() = transformData.pivot
    val scale: Vector2f
        get() = transformData.scale
    var rotation: Float
        get() = transformData.rotation
        set(value) { transformData.rotation = value }
    @JvmField var xVelocity: Float  = 0f
    @JvmField var yVelocity: Float  = 0f
    @JvmField var mass: Float  = 1f

    interface ParticleBuilder<P : Particle> {
        fun createEmpty(): P
    }

}