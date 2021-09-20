package com.inari.firefly.physics.movement

import com.inari.firefly.MovementAspect
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import kotlin.jvm.JvmField
import kotlin.math.pow
import kotlin.math.roundToInt

class EulerIntegrator private constructor() : Integrator() {

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

        velocity.dx = velocity.dx + acceleration.dx * deltaTimeInSeconds
        velocity.dy = velocity.dy + acceleration.dy * deltaTimeInSeconds
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        transform.move(
            (movement.velocity.dx * deltaTimeInSeconds * shift).roundToInt() / shift,
            (movement.velocity.dy * deltaTimeInSeconds * shift).roundToInt() / shift
        )
    }

    private fun gravityIntegration(movement: EMovement) {
        if (MovementAspect.ON_GROUND in movement.aspects) {
            movement.velocity.dy = 0f
            movement.acceleration.dy = 0f
        } else {
            if (movement.velocity.dy >= maxGravityVelocity) {
                movement.acceleration.dy = 0f
                movement.velocity.dy = maxGravityVelocity
                return
            }
            movement.acceleration.dy = gravity * (movement.mass * massFactor)
        }
    }

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, EulerIntegrator>(Integrator, EulerIntegrator::class) {
        override fun createEmpty() = EulerIntegrator()
    }
}