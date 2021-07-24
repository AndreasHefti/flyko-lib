package com.inari.firefly.physics.movement

import com.inari.firefly.core.system.SystemComponent
import com.inari.firefly.core.system.SystemComponentType
import com.inari.firefly.graphics.ETransform

abstract class Integrator protected constructor() : SystemComponent(Integrator::class.simpleName!!) {

    abstract fun integrate(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Long
    )

    abstract fun step(
        movement: EMovement,
        transform: ETransform,
        deltaTimeInSeconds: Long
    )

    override fun componentType() = Companion
    companion object : SystemComponentType<Integrator>(Integrator::class)
}