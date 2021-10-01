package com.inari.firefly.game.player.movement

import com.inari.firefly.core.system.SystemComponentSubType
import com.inari.firefly.graphics.ETransform
import com.inari.firefly.physics.movement.EMovement
import com.inari.firefly.physics.movement.Integrator
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField

class SimpleVelocityStepIntegrator  private constructor() : Integrator() {

    @JvmField var massFactor = 1f

    private val gravityAcc = Vector2f(0f, 0f)

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        gravityAcc(super.gravityVec)
        val accMass = 1 / movement.mass * massFactor
        gravityAcc / accMass

        movement.velocity.dx += deltaTimeInSeconds * gravityAcc.dx
        movement.velocity.dy += deltaTimeInSeconds * gravityAcc.dy

        adjustVelocity(movement)
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(
            movement.velocity.dx * deltaTimeInSeconds,
            movement.velocity.dy * deltaTimeInSeconds)

    override fun componentType() = Companion
    companion object : SystemComponentSubType<Integrator, SimpleVelocityStepIntegrator>(Integrator, SimpleVelocityStepIntegrator::class) {
        override fun createEmpty() = SimpleVelocityStepIntegrator()
    }
}