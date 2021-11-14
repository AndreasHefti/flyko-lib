package com.inari.firefly.physics.movement

import com.inari.firefly.MovementAspect.*
import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.graphics.ETransform
import com.inari.util.geom.Vector2f
import kotlin.jvm.JvmField
import kotlin.math.max
import kotlin.math.min

abstract class Integrator protected constructor() : SystemComponent(Integrator::class.simpleName!!) {

    @JvmField var gravityVec = Vector2f(0f, 9.8f)

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

    companion object : SystemComponentType<Integrator>(Integrator::class)
}