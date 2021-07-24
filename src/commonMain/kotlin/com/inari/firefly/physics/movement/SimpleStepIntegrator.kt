package com.inari.firefly.physics.movement

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import kotlin.math.abs

class SimpleStepIntegrator  private constructor() : Integrator() {

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Long) {
        val velocity = movement.velocity
        if (movement.mass == 0f)
            return

        if (movement.onGround) {
            if (velocity.dy != 0f )
                velocity.dy = 0f
            return
        }
       velocity.dy = velocity.dy + abs( (velocity.dy / movement.mass - 1f ) * 0.2f )
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Long) =
        transform.move(movement.velocity.dx, movement.velocity.dy)

    companion object : SystemComponentSubType<Integrator, SimpleStepIntegrator>(Integrator, SimpleStepIntegrator::class) {
        override fun createEmpty() = SimpleStepIntegrator()
    }
}