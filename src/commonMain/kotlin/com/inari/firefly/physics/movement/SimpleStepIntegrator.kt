package com.inari.firefly.physics.movement

import com.inari.firefly.MovementAspect
import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import kotlin.math.abs

class SimpleStepIntegrator  private constructor() : Integrator() {

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        val velocity = movement.velocity
        if (movement.mass == 0f)
            return

        if (MovementAspect.ON_GROUND in movement.aspects) {
            if (velocity.dy != 0f )
                velocity.dy = 0f
            return
        }
        velocity.dy = velocity.dy + abs( (velocity.dy / movement.mass - 1f ) * 0.2f )
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(movement.velocity.dx, movement.velocity.dy)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, SimpleStepIntegrator>(Integrator, SimpleStepIntegrator::class) {
        override fun createEmpty() = SimpleStepIntegrator()
    }
}