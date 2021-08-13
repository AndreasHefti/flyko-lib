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
            if (BLOCK_NORTH in movement.aspects && movement.velocity.dy < 0f)
                movement.velocity.dy = 0f
            if (BLOCK_EAST in movement.aspects && movement.velocity.dx > 0f)
                movement.velocity.dx = 0f
            if (BLOCK_SOUTH in movement.aspects && movement.velocity.dy > 0f)
                movement.velocity.dy = 0f
            if (BLOCK_WEST in movement.aspects && movement.velocity.dx < 0f)
                movement.velocity.dx = 0f
        }

        if (adjustGround && movement.onGround)
            movement.velocity.dy = 0f

        if (adjustMax) {
            movement.velocity.dy = min(movement.velocity.dy, movement.maxVelocitySouth)
            movement.velocity.dx = min(movement.velocity.dx, movement.maxVelocityEast)
            movement.velocity.dy = max(movement.velocity.dy, -movement.maxVelocityNorth)
            movement.velocity.dx = max(movement.velocity.dx, -movement.maxVelocityWest)
        }
    }

    override fun componentType() = Companion
    companion object : SystemComponentType<Integrator>(Integrator::class)
}