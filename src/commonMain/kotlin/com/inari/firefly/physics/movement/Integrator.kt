package com.inari.firefly.physics.movement

import com.inari.firefly.graphics.ETransform

interface Integrator {

    fun integrate(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Long
    )

    fun step(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Long
    )
}