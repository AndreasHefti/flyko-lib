package com.inari.firefly.game.player.movement

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Integrator
import kotlin.jvm.JvmField

class SemiImplicitEulerIntegrator private constructor() : Integrator() {

    @JvmField var massFactor = 1f

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {

        movement.acceleration(0f,0f)
        movement.acceleration + gravityVec
        movement.acceleration + movement.force
        val accMass = 1 / movement.mass * massFactor
        movement.acceleration / accMass

        movement.velocity.v0 += deltaTimeInSeconds * movement.acceleration.v0
        movement.velocity.v1 += deltaTimeInSeconds * movement.acceleration.v1

        adjustVelocity(movement)
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float)  =
        transform.move(
            movement.velocity.v0 * deltaTimeInSeconds,
            movement.velocity.v1 * deltaTimeInSeconds)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, SemiImplicitEulerIntegrator>(Integrator, SemiImplicitEulerIntegrator::class) {
        override fun createEmpty() = SemiImplicitEulerIntegrator()
    }
}