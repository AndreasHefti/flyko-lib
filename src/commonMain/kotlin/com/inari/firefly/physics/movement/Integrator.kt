package com.inari.firefly.physics.movement

import com.inari.firefly.graphics.view.ETransform
import com.inari.firefly.physics.movement.MovementControl.MovementAspect.*
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField
import kotlin.math.*

interface IntegratorBuilder<I : Integrator> {
    fun create(): I
}

abstract class Integrator {

    @JvmField
    var gravityVec = Vector2f(0f, 9.8f)
    @JvmField
    var adjustMax = true
    @JvmField
    var adjustGround = true
    @JvmField
    var adjustBlock = false

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

    override fun integrate(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) {
        val velocity = movement.velocity
        if (movement.mass == 0f)
            return

        if (ON_GROUND in movement.aspects) {
            if (velocity.v1 != 0f )
                velocity.v1 = 0f
            return
        }
        velocity.v1 = velocity.v1 + abs( (velocity.v1 / movement.mass - 1f ) * 0.2f )
    }

    override fun step(movement: EMovement, transform: ETransform, deltaTimeInSeconds: Float) =
        transform.move(movement.velocity.v0 * deltaTimeInSeconds, movement.velocity.v1 * deltaTimeInSeconds)

    companion object : IntegratorBuilder<SimpleStepIntegrator> {
        override fun create() = SimpleStepIntegrator()
    }
}

class EulerGravityIntegrator private constructor() : Integrator() {

    @JvmField
    var gravity = 9.8f
    @JvmField
    var maxGravityVelocity: Float = 1f
    @JvmField
    var massFactor: Float  = 1f
    @JvmField
    var shift = 10.0.pow(0.0).toFloat()
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
        if (ON_GROUND in movement.aspects || BLOCK_SOUTH in movement.aspects) {
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

    companion object : IntegratorBuilder<EulerGravityIntegrator> {
        override fun create() = EulerGravityIntegrator()
    }
}

