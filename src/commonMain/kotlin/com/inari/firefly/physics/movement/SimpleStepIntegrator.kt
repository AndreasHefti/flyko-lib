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
            if (velocity.v1 != 0f )
                velocity.v1 = 0f
            return
        }
        velocity.v1 = velocity.v1 + abs( (velocity.v1 / movement.mass - 1f ) * 0.2f )
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(movement.velocity.v0, movement.velocity.v1)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, SimpleStepIntegrator>(Integrator, SimpleStepIntegrator::class) {
        override fun createEmpty() = SimpleStepIntegrator()
    }
}