package com.inari.firefly.physics.movement

import com.inari.firefly.MovementAspect
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import kotlin.jvm.JvmField
import kotlin.math.pow
import kotlin.math.roundToInt

class EulerGravityIntegrator private constructor() : Integrator() {

    @JvmField var gravity = 9.8f
    @JvmField var maxGravityVelocity: Float = 1f
    @JvmField var massFactor: Float  = 1f
    @JvmField var shift = 10.0.pow(0.0).toFloat()
    var scale = -1
        set(value) {
            field = value
            shift = if (value < 0)
                10.0.pow(0.0).toFloat()
            else
                10.0.pow(value.toDouble()).toFloat()
        }

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        gravityIntegration( movement )
        val velocity = movement.velocity
        val acceleration = movement.acceleration

        velocity.v0 = velocity.v0 + acceleration.v0 * deltaTimeInSeconds
        velocity.v1 = velocity.v1 + acceleration.v1 * deltaTimeInSeconds
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        transform.move(
            (movement.velocity.v0 * deltaTimeInSeconds * shift).roundToInt() / shift,
            (movement.velocity.v1 * deltaTimeInSeconds * shift).roundToInt() / shift
        )
    }

    private fun gravityIntegration(movement: EMovement) {
        if (MovementAspect.ON_GROUND in movement.aspects || MovementAspect.BLOCK_SOUTH in movement.aspects) {
            movement.velocity.v1 = 0f
            movement.acceleration.v1 = 0f
        } else {
            if (movement.velocity.v1 >= maxGravityVelocity) {
                movement.acceleration.v1 = 0f
                movement.velocity.v1 = maxGravityVelocity
                return
            }
            movement.acceleration.v1 = gravity * (movement.mass * massFactor)
        }
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, EulerGravityIntegrator>(Integrator, EulerGravityIntegrator::class) {
        override fun createEmpty() = EulerGravityIntegrator()
    }
}