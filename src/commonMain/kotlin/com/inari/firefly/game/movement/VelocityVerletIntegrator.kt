package com.inari.firefly.game.movement

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Integrator
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class VelocityVerletIntegrator private constructor() : Integrator() {

    @JvmField var massFactor = 1f

    private val newAcc = Vector2f(0f, 0f)

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        // calc new acceleration
        newAcc(0f,0f) + gravityVec
        newAcc + movement.force
        val accMass = 1 / movement.mass * massFactor
        newAcc / accMass

        // velocity += timestep * (acceleration + newAcceleration) / 2;
        movement.velocity.dx += deltaTimeInSeconds * (movement.acceleration.dx + newAcc.dx) / 2
        movement.velocity.dy += deltaTimeInSeconds * (movement.acceleration.dy + newAcc.dy) / 2

        adjustVelocity(movement)
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        // position += timestep * (velocity + timestep * acceleration / 2);
        transform.move(
            deltaTimeInSeconds * (movement.velocity.dx + deltaTimeInSeconds * movement.acceleration.dx / 2),
            deltaTimeInSeconds * (movement.velocity.dy + deltaTimeInSeconds * movement.acceleration.dy / 2))

    companion object : SystemComponentSubType<Integrator, VelocityVerletIntegrator>(Integrator, VelocityVerletIntegrator::class) {
        override fun createEmpty() = VelocityVerletIntegrator()
    }
}