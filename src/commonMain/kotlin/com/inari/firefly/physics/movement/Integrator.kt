package com.inari.firefly.physics.movement

import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.Movement.MovementAspect.*
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

interface IntegratorBuilder<I : Integrator> {
    fun create(): I
    operator fun invoke(configure: I.() -> Unit): I = create().also(configure)
}

abstract class Integrator {

    @JvmField var gravityVec = Vector2f(0f, 9.8f)
    @JvmField var massFactor = 1f
    @JvmField var adjustMax = true
    @JvmField var adjustGround = true
    @JvmField var adjustBlock = false

    abstract fun integrate(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Float
    )

    abstract fun step(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Float
    )

    protected fun adjustVelocity(movement: EMovement) {

        if (adjustBlock) {
            if (BLOCK_NORTH in movement.aspects && movement.velocity.v1 < 0f)
                movement.velocity.v1 = 0f
            if (BLOCK_EAST in movement.aspects && movement.velocity.v0 > 0f)
                movement.velocity.v0 = 0f
            if (BLOCK_SOUTH in movement.aspects && movement.velocity.v1 > 0f)
                movement.velocity.v1 = 0f
            if (BLOCK_WEST in movement.aspects && movement.velocity.v0 < 0f)
                movement.velocity.v0 = 0f
        }

        if (adjustGround && movement.onGround)
            movement.velocity.v1 = 0f

        if (adjustMax) {
            movement.velocity.v1 = min(movement.velocity.v1, movement.maxVelocitySouth)
            movement.velocity.v0 = min(movement.velocity.v0, movement.maxVelocityEast)
            movement.velocity.v1 = max(movement.velocity.v1, -movement.maxVelocityNorth)
            movement.velocity.v0 = max(movement.velocity.v0, -movement.maxVelocityWest)
        }
    }
}

class SimpleStepIntegrator private constructor() : Integrator() {

    private val gravityAcc = Vector2f(0f, 0f)

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        gravityAcc(super.gravityVec)
        val accMass = 1 / movement.mass * massFactor
        gravityAcc / accMass

        movement.velocity.v0 += deltaTimeInSeconds * gravityAcc.v0
        movement.velocity.v1 += deltaTimeInSeconds * gravityAcc.v1

        adjustVelocity(movement)
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(
            movement.velocity.v0 * deltaTimeInSeconds,
            movement.velocity.v1 * deltaTimeInSeconds)

    companion object : IntegratorBuilder<SimpleStepIntegrator> {
        override fun create() = SimpleStepIntegrator()
    }
}

class VelocityVerletIntegrator private constructor() : Integrator() {

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

    companion object : IntegratorBuilder<VelocityVerletIntegrator> {
        override fun create() = VelocityVerletIntegrator()
    }
}

class EulerGravityIntegrator private constructor() : Integrator() {

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

    companion object : IntegratorBuilder<EulerGravityIntegrator> {
        override fun create() = EulerGravityIntegrator()
    }
}

