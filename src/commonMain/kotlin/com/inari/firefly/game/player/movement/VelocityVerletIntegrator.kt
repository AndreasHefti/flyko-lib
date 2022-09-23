package com.inari.firefly.game.player.movement

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

        movement.velocity.v0 += deltaTimeInSeconds * (movement.acceleration.v0 + newAcc.v0) / 2
        movement.velocity.v1 += deltaTimeInSeconds * (movement.acceleration.v1 + newAcc.v1) / 2

        adjustVelocity(movement)
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(
            deltaTimeInSeconds * (movement.velocity.v0 + deltaTimeInSeconds * movement.acceleration.v0 / 2),
            deltaTimeInSeconds * (movement.velocity.v1 + deltaTimeInSeconds * movement.acceleration.v1 / 2))

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, VelocityVerletIntegrator>(Integrator, VelocityVerletIntegrator::class) {
        override fun createEmpty() = VelocityVerletIntegrator()
    }
}